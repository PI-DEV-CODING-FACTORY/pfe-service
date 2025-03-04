import AWS from 'aws-sdk';
import { MilvusClient } from '@zilliz/milvus2-sdk-node';
import OpenAI from 'openai';
import { Pool } from '@neondatabase/serverless';
import pdf from 'pdf-parse';

if (!process.env.OPENAI_API_KEY) {
  throw new Error('Missing OPENAI_API_KEY environment variable');
}
if (!process.env.MILVUS_URI) {
  throw new Error('Missing MILVUS_URI environment variable');
}
if (!process.env.MILVUS_TOKEN) {
  throw new Error('Missing MILVUS_TOKEN environment variable');
}

const pool = new Pool({ connectionString: process.env.NEON_CONNECTION_STRING });
const s3 = new AWS.S3();
const openai = new OpenAI({
  apiKey: process.env.OPENAI_API_KEY,
});
const milvusClient = new MilvusClient({
  address: process.env.MILVUS_URI,
  token: process.env.MILVUS_TOKEN,
});

const collectionName = 'pfe_rapports';

const splitText = (text, chunkSize) => {
  const chunks = [];
  let currentChunk = '';
  let currentLength = 0;

  const words = text.split(' ');
  for (const word of words) {
    const wordLength = word.length + 1;
    if (currentLength + wordLength <= chunkSize) {
      currentChunk += word + ' ';
      currentLength += wordLength;
    } else {
      chunks.push(currentChunk.trim());
      currentChunk = word + ' ';
      currentLength = wordLength;
    }
  }
  if (currentChunk) {
    chunks.push(currentChunk.trim());
  }
  return chunks;
};

const generateProjectResume = async (text) => {
  try {
    const response = await openai.chat.completions.create({
      model: "gpt-4-turbo-preview",
      messages: [
        {
          role: "system",
          content: `You are a technical analyst specialized in analyzing final year project reports.
Create a concise resume of the project with the following structure:
1. Project Overview (2-3 sentences)
2. Main Objectives (bullet points)
3. Technologies Used (only mention technologies from the Technologies enum)
4. Key Features Implemented (bullet points)
5. Methodology/Approach (2-3 sentences)
6. Results and Outcomes (2-3 sentences)

Keep the resume focused and technical. Return the response in a clean markdown format.`
        },
        {
          role: "user",
          content: text
        }
      ],
      temperature: 0.3,
      max_tokens: 1000,
    });

    return response.choices[0].message.content;
  } catch (error) {
    console.error('Error generating project resume:', error);
    return null;
  }
};

const extractTechnologies = async (text) => {
  try {
    const response = await openai.chat.completions.create({
      model: "gpt-4-turbo-preview",
      messages: [
        {
          role: "system",
          content: "You are a technical analyzer. From the given text, identify technologies mentioned. Return only the technology names that match the Technologies enum in the format: TECHNOLOGY_NAME,ANOTHER_TECHNOLOGY. If no valid technologies are found, return an empty string."
        },
        {
          role: "user",
          content: text
        }
      ],
      temperature: 0.3,
    });

    const technologies = response.choices[0].message.content.split(',').filter(tech => tech.trim() !== '');
    return technologies;
  } catch (error) {
    console.error('Error extracting technologies:', error);
    return [];
  }
};

export const handler = async (event) => {
  const bucket = event.Records[0].s3.bucket.name;
  let key = decodeURIComponent(event.Records[0].s3.object.key);

  const keyParts = key.split('/');
  if (keyParts.length !== 2 || !keyParts[1].includes('_')) {
    console.error('Invalid key format');
    return;
  }

  const [pfeId, objectId] = keyParts[1].split('_');

  try {
    // Retrieve the PDF from S3
    const response = await s3.getObject({ Bucket: bucket, Key: key }).promise();
    
    // Parse PDF content
    const pdfData = await pdf(response.Body);
    const fileContent = pdfData.text;

    // Check if we successfully extracted text
    if (!fileContent || fileContent.trim().length === 0) {
      throw new Error('Failed to extract text from PDF');
    }

    // Generate project resume
    const resume = await generateProjectResume(fileContent);
    if (!resume) {
      throw new Error('Failed to generate project resume');
    }

    // Extract technologies from the content
    const technologies = await extractTechnologies(fileContent);

    // Create embedding for the resume
    const embeddingResponse = await openai.embeddings.create({
      input: resume,
      model: 'text-embedding-3-small',
    });

    if (!embeddingResponse?.data?.[0]?.embedding) {
      throw new Error('Failed to generate embedding for resume');
    }

    const embeddings = embeddingResponse.data[0].embedding;

    // Save resume and its vector to Milvus
    await milvusClient.insert({
      collection_name: collectionName,
      data: [{
        pfe_id: pfeId,
        object_id: objectId,
        vector: embeddings,
        content: resume
      }],
    });

    // Update PFE with technologies and resume
    await pool.query(
      'UPDATE pfe SET technologies = $1::technologies[], processing = false, resume = $2 WHERE id = $3',
      [technologies, resume, pfeId]
    );

    await pool.end();
    
    return {
      statusCode: 200,
      body: JSON.stringify({
        message: 'Rapport processed successfully',
        technologies: technologies,
        resume: resume
      })
    };

  } catch (error) {
    console.error(`Error processing rapport:`, error);
    // Update PFE to mark processing as failed
    try {
      await pool.query(
        'UPDATE pfe SET processing = false WHERE id = $1',
        [pfeId]
      );
      await pool.end();
    } catch (dbError) {
      console.error('Error updating processing status:', dbError);
    }
    throw error;
  }
}; 