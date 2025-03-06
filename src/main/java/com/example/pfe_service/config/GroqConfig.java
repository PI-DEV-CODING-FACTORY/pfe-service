package com.example.pfe_service.config;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

@Configuration
public class GroqConfig {
    @Value("${groq.api.key}")
    private String apiKey;

    @Bean
    public Retrofit groqRetrofit() {
        OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(chain -> {
                Request original = chain.request();
                Request request = original.newBuilder()
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .method(original.method(), original.body())
                    .build();
                return chain.proceed(request);
            })
            .build();

        return new Retrofit.Builder()
            .baseUrl("https://api.groq.com/openai/v1/")
            .client(client)
            .addConverterFactory(JacksonConverterFactory.create())
            .build();
    }
} 