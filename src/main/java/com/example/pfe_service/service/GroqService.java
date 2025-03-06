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
import java.util.stream.Collectors;

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
                // Try one more time with a different temperature
                request = GroqRequest.builder()
                        .model("mixtral-8x7b-32768")
                        .messages(List.of(Map.of("role", "user", "content", prompt)))
                        .temperature(0.9) // Higher temperature for more creativity
                        .stream(false)
                        .build();
                retrofitResponse = api.chat(request).execute();
                
                if (!retrofitResponse.isSuccessful()) {
                    log.warn("Second attempt failed, using fallback questions");
                    return generateFallbackQuestions(technologies);
                }
            }
            
            GroqResponse response = retrofitResponse.body();
            
            if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
                log.error("Groq API returned invalid response: {}", response);
                return generateFallbackQuestions(technologies);
            }
            
            String content = response.getChoices().get(0).getMessage().get("content");
            
            if (content == null || content.isEmpty()) {
                log.error("Groq API returned empty content");
                return generateFallbackQuestions(technologies);
            }
            
            log.info("Successfully received response from Groq API");
            log.debug("Response content: {}", content);
            
            try {
                List<Question> questions = parseQuestions(content);
                
                // Validate the generated questions
                if (questions.size() < 5 || !validateQuestions(questions, technologies)) {
                    log.warn("Generated questions did not meet requirements, trying again with different prompt");
                    // Try again with a modified prompt
                    String retryPrompt = prompt + "\n\nIMPORTANT: Please ensure to generate exactly 5 diverse questions covering the specified technologies. Do not reuse common examples.";
                    request = buildRequest(retryPrompt);
                    retrofitResponse = api.chat(request).execute();
                    
                    if (retrofitResponse.isSuccessful() && retrofitResponse.body() != null) {
                        content = retrofitResponse.body().getChoices().get(0).getMessage().get("content");
                        questions = parseQuestions(content);
                        
                        if (questions.size() >= 5 && validateQuestions(questions, technologies)) {
                            return questions;
                        }
                    }
                    
                    log.warn("Retry attempt failed to generate valid questions");
                    return generateFallbackQuestions(technologies);
                }
                
                return questions;
            } catch (Exception e) {
                log.error("Error parsing questions: {}", e.getMessage(), e);
                return generateFallbackQuestions(technologies);
            }
        } catch (Exception e) {
            log.error("Error generating technical questions: {}", e.getMessage(), e);
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
        StringBuilder techSpecificInstructions = new StringBuilder();
        
        // Add specific instructions for each technology
        for (Technologies tech : technologies) {
            switch (tech) {
                case JAVA:
                    techSpecificInstructions.append("""
                        For Java questions:
                        - Include core Java concepts (OOP, Collections, Streams)
                        - Cover Spring Boot if mentioned in the description
                        - Include questions about memory management and JVM
                        - Focus on Java 8+ features if possible
                        """);
                    break;
                case SPRING_BOOT:
                    techSpecificInstructions.append("""
                        For Spring Boot questions:
                        - Cover dependency injection and Spring IoC
                        - Include questions about REST APIs and controllers
                        - Ask about Spring Data JPA and repositories
                        - Include Spring Security concepts if relevant
                        """);
                    break;
                case JAVASCRIPT:
                    techSpecificInstructions.append("""
                        For JavaScript questions:
                        - Focus on ES6+ features
                        - Cover async programming (Promises, async/await)
                        - Include DOM manipulation if frontend-related
                        - Ask about closures and scope
                        """);
                    break;
                case TYPESCRIPT:
                    techSpecificInstructions.append("""
                        For TypeScript questions:
                        - Cover type system and interfaces
                        - Include questions about generics
                        - Ask about TypeScript-specific features
                        - Focus on type safety and best practices
                        """);
                    break;
                case ANGULAR:
                    techSpecificInstructions.append("""
                        For Angular questions:
                        - Cover components and services
                        - Include questions about dependency injection
                        - Ask about Angular lifecycle hooks
                        - Include RxJS and Observables
                        """);
                    break;
                case REACT:
                    techSpecificInstructions.append("""
                        For React questions:
                        - Cover hooks and functional components
                        - Include state management concepts
                        - Ask about component lifecycle
                        - Include questions about React Router and Context
                        """);
                    break;
                case EXPRESS_JS:
                    techSpecificInstructions.append("""
                        For Express.js questions:
                        - Cover middleware and routing
                        - Include REST API design
                        - Ask about error handling
                        - Include authentication and security
                        """);
                    break;
                case POSTGRESQL:
                case MYSQL:
                    techSpecificInstructions.append("""
                        For SQL Database questions:
                        - Cover query optimization
                        - Include transaction management
                        - Ask about indexing strategies
                        - Include data modeling best practices
                        """);
                    break;
                case MONGODB:
                    techSpecificInstructions.append("""
                        For MongoDB questions:
                        - Cover document design
                        - Include aggregation pipeline
                        - Ask about indexing in MongoDB
                        - Include scaling and sharding concepts
                        """);
                    break;
                case DOCKER:
                    techSpecificInstructions.append("""
                        For Docker questions:
                        - Cover containerization concepts
                        - Include Dockerfile best practices
                        - Ask about container orchestration
                        - Include Docker networking
                        """);
                    break;
                case KUBERNETES:
                    techSpecificInstructions.append("""
                        For Kubernetes questions:
                        - Cover pod lifecycle
                        - Include deployment strategies
                        - Ask about service discovery
                        - Include scaling and load balancing
                        """);
                    break;
                default:
                    techSpecificInstructions.append(String.format("""
                        For %s questions:
                        - Focus on fundamental concepts
                        - Include practical application scenarios
                        - Cover best practices and common patterns
                        - Relate to real-world use cases
                        """, tech.name()));
            }
        }

        return String.format("""
            Generate 5 technical interview questions for a PFE (Final Year Project) position.
            The questions should specifically test the candidate's knowledge of the following technologies: %s
            
            Project Description: %s
            
            %s
            
            Question Distribution:
            - Ensure at least one question for each technology mentioned
            - Make 60%% of questions specific to the primary technologies
            - Include both theoretical and practical questions
            - Relate questions to the project description when possible
            
            Question Difficulty:
            - 20%% Easy (1-2 points)
            - 60%% Medium (3 points)
            - 20%% Hard (4-5 points)
            
            For each question, provide:
            1. The question text
            2. Whether it's multiple choice (true/false)
            3. For multiple choice: exactly 4 options with only one correct answer
            4. The correct answer
            5. A detailed explanation that helps the student learn
            6. Points (1-5 based on difficulty)
            7. The primary technology being tested
            
            Format the response as a JSON array of objects with the following structure:
            [
              {
                "text": "question text",
                "isMultipleChoice": boolean,
                "options": ["option1", "option2", "option3", "option4"] (only for multiple choice),
                "correctAnswer": "correct answer",
                "explanation": "explanation text",
                "points": number,
                "technology": "primary technology being tested"
              }
            ]
            """,
            technologies.stream()
                    .map(Enum::name)
                    .collect(Collectors.joining(", ")),
            description,
            techSpecificInstructions.toString()
        );
    }

    private GroqRequest buildRequest(String prompt) {
        return GroqRequest.builder()
                .model("llama-3.3-70b-versatile")
                .messages(List.of(Map.of("role", "user", "content", prompt)))
                .temperature(0.7)
                .maxTokens(4000)
                .stream(false)
                .build();
    }

    private List<Question> parseQuestions(String content) {
        try {
            log.debug("Parsing questions from content: {}", content);
            
            // Clean up the content to ensure valid JSON
            content = content.trim();
            if (!content.startsWith("[")) {
                // Extract JSON array if it's embedded in text
                int start = content.indexOf("[");
                int end = content.lastIndexOf("]") + 1;
                if (start >= 0 && end > start) {
                    content = content.substring(start, end);
                }
            }
            
            List<Map<String, Object>> questionsData = objectMapper.readValue(
                content, new TypeReference<List<Map<String, Object>>>() {});
            
            log.debug("Parsed {} question data objects", questionsData.size());
            
            List<Question> questions = new ArrayList<>();
            for (Map<String, Object> data : questionsData) {
                try {
                    Question question = new Question();
                    question.setText((String) data.get("text"));
                    question.setIsMultipleChoice((Boolean) data.get("isMultipleChoice"));
                    
                    if (question.getIsMultipleChoice()) {
                        Object optionsObj = data.get("options");
                        if (optionsObj instanceof List) {
                            @SuppressWarnings("unchecked")
                            List<String> optionsList = (List<String>) optionsObj;
                            question.setOptions(optionsList.toArray(new String[0]));
                        } else if (optionsObj instanceof String[]) {
                            question.setOptions((String[]) optionsObj);
                        }
                    }
                    
                    question.setCorrectAnswer((String) data.get("correctAnswer"));
                    question.setExplanation((String) data.get("explanation"));
                    
                    // Handle points with type conversion if necessary
                    Object pointsObj = data.get("points");
                    if (pointsObj instanceof Integer) {
                        question.setPoints((Integer) pointsObj);
                    } else if (pointsObj instanceof Number) {
                        question.setPoints(((Number) pointsObj).intValue());
                    } else if (pointsObj instanceof String) {
                        question.setPoints(Integer.parseInt((String) pointsObj));
                    }
                    
                    // Set technology field
                    String technology = (String) data.get("technology");
                    if (technology != null) {
                        question.setTechnology(technology.toUpperCase());
                    }
                    
                    question.setIsCorrect(false); // Initialize as not answered yet
                    
                    // Validate the question before adding
                    if (isValidQuestion(question)) {
                        questions.add(question);
                    } else {
                        log.warn("Skipping invalid question: {}", question.getText());
                    }
                } catch (Exception e) {
                    log.error("Error parsing individual question: {}", e.getMessage());
                }
            }
            
            if (questions.isEmpty()) {
                throw new RuntimeException("No valid questions could be parsed");
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

    private boolean isValidQuestion(Question question) {
        if (question.getText() == null || question.getText().trim().isEmpty()) {
            return false;
        }
        
        if (question.getCorrectAnswer() == null || question.getCorrectAnswer().trim().isEmpty()) {
            return false;
        }
        
        if (question.getPoints() == null || question.getPoints() < 1 || question.getPoints() > 5) {
            return false;
        }
        
        if (question.getIsMultipleChoice()) {
            if (question.getOptions() == null || question.getOptions().length != 4) {
                return false;
            }
            
            // Check if correct answer is one of the options
            boolean correctAnswerInOptions = false;
            for (String option : question.getOptions()) {
                if (option.equals(question.getCorrectAnswer())) {
                    correctAnswerInOptions = true;
                    break;
                }
            }
            if (!correctAnswerInOptions) {
                return false;
            }
        }
        
        if (question.getTechnology() == null || question.getTechnology().trim().isEmpty()) {
            return false;
        }
        
        return true;
    }

    public boolean verifyAnswer(String correctAnswer, String userAnswer, String question) {
        try {
            log.info("Verifying answer for question: {}", question);
            
            String prompt = String.format("""
                You are an AI evaluating a student's answer to a technical question.
                
                Question: %s
                Correct Answer: %s
                Student's Answer: %s
                
                Evaluate if the student's answer is correct. Consider:
                1. Core concepts and technical accuracy
                2. Key points from the correct answer
                3. Different ways of expressing the same concept
                
                Respond with only 'true' if the answer is correct, or 'false' if it's incorrect.
                """, question, correctAnswer, userAnswer);
            
            GroqRequest request = buildRequest(prompt);
            GroqApi api = retrofit.create(GroqApi.class);
            
            Response<GroqResponse> retrofitResponse = api.chat(request).execute();
            
            if (!retrofitResponse.isSuccessful() || retrofitResponse.body() == null || 
                retrofitResponse.body().getChoices() == null || 
                retrofitResponse.body().getChoices().isEmpty()) {
                log.error("Failed to verify answer using Groq API");
                // If API fails, be lenient and mark as correct
                return true;
            }
            
            String response = retrofitResponse.body().getChoices().get(0).getMessage().get("content").trim().toLowerCase();
            return response.equals("true");
            
        } catch (Exception e) {
            log.error("Error verifying answer: {}", e.getMessage());
            // If there's an error, be lenient and mark as correct
            return true;
        }
    }

    private boolean validateQuestions(List<Question> questions, List<Technologies> technologies) {
        if (questions.size() < 5) {
            return false;
        }

        // Check if we have at least one question for each primary technology
        Set<String> coveredTechnologies = questions.stream()
                .map(q -> q.getTechnology())
                .collect(Collectors.toSet());

        // Convert technology enums to strings for comparison
        Set<String> requiredTechnologies = technologies.stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        // Check if we have questions for at least 60% of the required technologies
        long technologiesCovered = requiredTechnologies.stream()
                .filter(tech -> coveredTechnologies.stream()
                        .anyMatch(covered -> covered.toUpperCase().contains(tech)))
                .count();

        double coveragePercentage = (double) technologiesCovered / requiredTechnologies.size();
        if (coveragePercentage < 0.6) {
            return false;
        }

        // Check for question diversity (no duplicate questions)
        long uniqueQuestions = questions.stream()
                .map(Question::getText)
                .distinct()
                .count();
        if (uniqueQuestions < questions.size()) {
            return false;
        }

        // Validate multiple choice questions have exactly 4 options
        boolean validOptions = questions.stream()
                .filter(Question::getIsMultipleChoice)
                .allMatch(q -> q.getOptions() != null && q.getOptions().length == 4);

        return validOptions;
    }

    private interface GroqApi {
        @POST("chat/completions")
        retrofit2.Call<GroqResponse> chat(@Body GroqRequest request);
    }
} 