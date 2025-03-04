package com.example.pfe_service.service;

import com.example.pfe_service.dto.GroqRequest;
import com.example.pfe_service.dto.GroqResponse;
import com.example.pfe_service.entities.Question;
import com.example.pfe_service.entities.Technologies;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.Body;
import retrofit2.http.POST;

import java.io.IOException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroqService {
    private final Retrofit retrofit;
    private final ObjectMapper objectMapper;

    public List<Question> generateTechnicalQuestions(Set<Technologies> technologies, String description) {
        return generateTechnicalQuestions(new ArrayList<>(technologies), description);
    }

    public List<Question> generateTechnicalQuestions(List<Technologies> technologies, String description) {
        try {
            log.info("Generating technical questions for technologies: {}", technologies);
            
            String prompt = buildPrompt(technologies, description);
            log.debug("Generated prompt: {}", prompt);
            
            GroqRequest request = buildRequest(prompt);
            log.debug("Built request with model: {}", request.getModel());
            
            GroqApi api = retrofit.create(GroqApi.class);
            log.info("Sending request to Groq API");
            
            Response<GroqResponse> retrofitResponse = api.chat(request).execute();
            
            if (!retrofitResponse.isSuccessful()) {
                String errorBody = retrofitResponse.errorBody() != null ? retrofitResponse.errorBody().string() : "Unknown error";
                log.error("Groq API request failed with code: {}, error: {}", retrofitResponse.code(), errorBody);
                log.warn("Using fallback questions instead");
                return generateFallbackQuestions(technologies);
            }
            
            GroqResponse response = retrofitResponse.body();
            
            if (response == null) {
                log.error("Groq API returned null response");
                log.warn("Using fallback questions instead");
                return generateFallbackQuestions(technologies);
            }
            
            if (response.getChoices() == null || response.getChoices().isEmpty()) {
                log.error("Groq API returned empty choices: {}", response);
                log.warn("Using fallback questions instead");
                return generateFallbackQuestions(technologies);
            }
            
            String content = response.getChoices().get(0).getMessage().get("content");
            
            if (content == null || content.isEmpty()) {
                log.error("Groq API returned empty content");
                log.warn("Using fallback questions instead");
                return generateFallbackQuestions(technologies);
            }
            
            log.info("Successfully received response from Groq API");
            log.debug("Response content: {}", content);
            
            List<Question> questions = parseQuestions(content);
            log.info("Successfully parsed {} questions", questions.size());
            
            return questions;
        } catch (IOException e) {
            log.error("Error generating technical questions: {}", e.getMessage(), e);
            log.warn("Using fallback questions instead");
            return generateFallbackQuestions(technologies);
        } catch (Exception e) {
            log.error("Unexpected error generating technical questions: {}", e.getMessage(), e);
            log.warn("Using fallback questions instead");
            return generateFallbackQuestions(technologies);
        }
    }
    
    private List<Question> generateFallbackQuestions(List<Technologies> technologies) {
        log.info("Generating fallback questions for technologies: {}", technologies);
        
        List<Question> fallbackQuestions = new ArrayList<>();
        
        // Create a map of technology-specific questions
        Map<Technologies, List<Question>> techQuestions = new HashMap<>();
        
        // Java questions
        if (technologies.contains(Technologies.JAVA)) {
            Question q1 = new Question();
            q1.setText("What is the difference between '==' and '.equals()' in Java?");
            q1.setIsMultipleChoice(false);
            q1.setCorrectAnswer("'==' compares object references (memory addresses), while '.equals()' compares the content/values of objects.");
            q1.setExplanation("In Java, '==' is used to compare primitive types or to check if two references point to the same object. The '.equals()' method is used to compare the contents of objects. For example, two different String objects with the same characters would return true with .equals() but false with ==.");
            q1.setPoints(3);
            q1.setIsCorrect(false);
            
            Question q2 = new Question();
            q2.setText("Which of the following is NOT a feature of Java?");
            q2.setIsMultipleChoice(true);
            q2.setOptions(new String[]{
                "Platform independence", 
                "Automatic memory management", 
                "Multiple inheritance of classes", 
                "Object-oriented programming"
            });
            q2.setCorrectAnswer("Multiple inheritance of classes");
            q2.setExplanation("Java does not support multiple inheritance of classes to avoid the 'diamond problem'. However, it does support multiple inheritance of interfaces through the implements keyword.");
            q2.setPoints(2);
            q2.setIsCorrect(false);
            
            techQuestions.put(Technologies.JAVA, Arrays.asList(q1, q2));
        }
        
        // JavaScript questions
        if (technologies.contains(Technologies.JAVASCRIPT) || technologies.contains(Technologies.TYPESCRIPT)) {
            Question q1 = new Question();
            q1.setText("What is the difference between 'let', 'const', and 'var' in JavaScript?");
            q1.setIsMultipleChoice(false);
            q1.setCorrectAnswer("'var' is function-scoped and can be redeclared, 'let' is block-scoped and can be reassigned but not redeclared, 'const' is block-scoped and cannot be reassigned or redeclared.");
            q1.setExplanation("'var' declarations are globally or function scoped and can be redeclared and updated. 'let' declarations are block scoped and can be updated but not redeclared. 'const' declarations are block scoped and cannot be updated or redeclared.");
            q1.setPoints(3);
            q1.setIsCorrect(false);
            
            Question q2 = new Question();
            q2.setText("Which of the following is true about closures in JavaScript?");
            q2.setIsMultipleChoice(true);
            q2.setOptions(new String[]{
                "Closures can only access global variables", 
                "Closures allow a function to access variables from an outer function after the outer function has returned", 
                "Closures are only available in ES6 and later", 
                "Closures prevent memory leaks in all cases"
            });
            q2.setCorrectAnswer("Closures allow a function to access variables from an outer function after the outer function has returned");
            q2.setExplanation("A closure is the combination of a function and the lexical environment within which that function was declared. This allows the function to access variables from its outer scope even after the outer function has returned.");
            q2.setPoints(4);
            q2.setIsCorrect(false);
            
            techQuestions.put(Technologies.JAVASCRIPT, Arrays.asList(q1, q2));
        }
        
        // React questions
        if (technologies.contains(Technologies.REACT)) {
            Question q1 = new Question();
            q1.setText("What is the purpose of React's virtual DOM?");
            q1.setIsMultipleChoice(false);
            q1.setCorrectAnswer("The virtual DOM is a lightweight copy of the actual DOM that React uses to improve performance by minimizing direct DOM manipulations.");
            q1.setExplanation("React creates a virtual representation of the UI in memory (virtual DOM), which it uses to determine what changes need to be made to the actual DOM. This approach is more efficient than directly manipulating the DOM for every state change.");
            q1.setPoints(3);
            q1.setIsCorrect(false);
            
            Question q2 = new Question();
            q2.setText("Which hook would you use to perform side effects in a functional component?");
            q2.setIsMultipleChoice(true);
            q2.setOptions(new String[]{
                "useState", 
                "useEffect", 
                "useContext", 
                "useReducer"
            });
            q2.setCorrectAnswer("useEffect");
            q2.setExplanation("The useEffect hook is used to perform side effects in functional components. Side effects include data fetching, subscriptions, manual DOM manipulations, and other operations that affect components outside the current render cycle.");
            q2.setPoints(2);
            q2.setIsCorrect(false);
            
            techQuestions.put(Technologies.REACT, Arrays.asList(q1, q2));
        }
        
        // Spring Boot questions
        if (technologies.contains(Technologies.SPRING_BOOT)) {
            Question q1 = new Question();
            q1.setText("What is the purpose of the @Autowired annotation in Spring?");
            q1.setIsMultipleChoice(false);
            q1.setCorrectAnswer("@Autowired is used for automatic dependency injection, allowing Spring to resolve and inject collaborating beans into your bean.");
            q1.setExplanation("The @Autowired annotation in Spring is used to automatically inject dependencies. It can be applied to fields, setter methods, or constructors. Spring will look for a matching bean definition and wire it in at runtime.");
            q1.setPoints(3);
            q1.setIsCorrect(false);
            
            Question q2 = new Question();
            q2.setText("Which of the following is NOT a feature of Spring Boot?");
            q2.setIsMultipleChoice(true);
            q2.setOptions(new String[]{
                "Auto-configuration", 
                "Embedded server support", 
                "Manual XML configuration requirement", 
                "Production-ready features"
            });
            q2.setCorrectAnswer("Manual XML configuration requirement");
            q2.setExplanation("Spring Boot aims to minimize configuration, especially XML configuration. It uses auto-configuration, which automatically configures your application based on the dependencies you have added. XML configuration is optional, not required.");
            q2.setPoints(2);
            q2.setIsCorrect(false);
            
            techQuestions.put(Technologies.SPRING_BOOT, Arrays.asList(q1, q2));
        }
        
        // Add generic programming questions if no specific technology matches or as additional questions
        Question generic1 = new Question();
        generic1.setText("What is the time complexity of binary search?");
        generic1.setIsMultipleChoice(true);
        generic1.setOptions(new String[]{
            "O(1)", 
            "O(log n)", 
            "O(n)", 
            "O(n²)"
        });
        generic1.setCorrectAnswer("O(log n)");
        generic1.setExplanation("Binary search has a time complexity of O(log n) because it repeatedly divides the search interval in half. If the array has n elements, the algorithm takes at most log₂(n) steps to find the target element.");
        generic1.setPoints(3);
        generic1.setIsCorrect(false);
        
        Question generic2 = new Question();
        generic2.setText("What is the difference between a stack and a queue?");
        generic2.setIsMultipleChoice(false);
        generic2.setCorrectAnswer("A stack follows Last-In-First-Out (LIFO) order, while a queue follows First-In-First-Out (FIFO) order.");
        generic2.setExplanation("In a stack, elements are added and removed from the same end (like a stack of plates), following LIFO order. In a queue, elements are added at one end and removed from the other (like a line of people), following FIFO order.");
        generic2.setPoints(2);
        generic2.setIsCorrect(false);
        
        // Add questions to the fallback list
        // First, add technology-specific questions
        for (Technologies tech : technologies) {
            if (techQuestions.containsKey(tech)) {
                fallbackQuestions.addAll(techQuestions.get(tech));
                if (fallbackQuestions.size() >= 4) {
                    break; // Limit to 4 technology-specific questions
                }
            }
        }
        
        // Add generic questions to reach 5 total questions
        if (fallbackQuestions.size() < 5) {
            fallbackQuestions.add(generic1);
        }
        if (fallbackQuestions.size() < 5) {
            fallbackQuestions.add(generic2);
        }
        
        // Limit to 5 questions
        if (fallbackQuestions.size() > 5) {
            fallbackQuestions = fallbackQuestions.subList(0, 5);
        }
        
        log.info("Generated {} fallback questions", fallbackQuestions.size());
        return fallbackQuestions;
    }

    private String buildPrompt(List<Technologies> technologies, String description) {
        return String.format("""
            Generate 5 technical interview questions based on the following technologies and project description.
            Make sure the questions are challenging and relevant to assess a student's proficiency in these technologies.
            
            Technologies: %s
            Project Description: %s
            
            For each question:
            1. Make it specific to one of the listed technologies
            2. Ensure it's relevant to the project description when possible
            3. Include a mix of theoretical knowledge and practical application
            4. For multiple-choice questions, include 4 options with only one correct answer
            5. For non-multiple-choice questions, expect a concise answer
            
            For each question, provide:
            1. The question text
            2. Whether it's multiple choice (true/false)
            3. The correct answer
            4. A detailed explanation of the answer that helps the student learn
            5. Points (between 1-5 based on difficulty)
            
            Format the response as a JSON array of objects with the following structure:
            [
              {
                "text": "question text",
                "isMultipleChoice": boolean,
                "options": ["option1", "option2", "option3", "option4"] (only for multiple choice),
                "correctAnswer": "correct answer",
                "explanation": "explanation text",
                "points": number
              }
            ]
            """,
            technologies.stream()
                    .map(Enum::name)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse(""),
            description
        );
    }

    private GroqRequest buildRequest(String prompt) {
        return GroqRequest.builder()
                .model("mixtral-8x7b-32768")
                .messages(List.of(Map.of("role", "user", "content", prompt)))
                .temperature(0.7)
                .maxTokens(4000)
                .stream(false)
                .build();
    }

    private List<Question> parseQuestions(String content) {
        try {
            log.debug("Parsing questions from content: {}", content);
            
            List<Map<String, Object>> questionsData = objectMapper.readValue(
                content, new TypeReference<List<Map<String, Object>>>() {});
            
            log.debug("Parsed {} question data objects", questionsData.size());
            
            List<Question> questions = new ArrayList<>();
            for (Map<String, Object> data : questionsData) {
                Question question = new Question();
                question.setText((String) data.get("text"));
                question.setIsMultipleChoice((Boolean) data.get("isMultipleChoice"));
                question.setOptions(data.get("options") != null ? 
                    objectMapper.convertValue(data.get("options"), String[].class) : null);
                question.setCorrectAnswer((String) data.get("correctAnswer"));
                question.setExplanation((String) data.get("explanation"));
                question.setPoints((Integer) data.get("points"));
                question.setIsCorrect(false); // Initialize as not answered yet
                questions.add(question);
            }
            
            return questions;
        } catch (JsonProcessingException e) {
            log.error("Error parsing questions from Groq response: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse questions: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error parsing questions: {}", e.getMessage(), e);
            throw new RuntimeException("Unexpected error parsing questions: " + e.getMessage(), e);
        }
    }

    private interface GroqApi {
        @POST("chat/completions")
        retrofit2.Call<GroqResponse> chat(@Body GroqRequest request);
    }
} 