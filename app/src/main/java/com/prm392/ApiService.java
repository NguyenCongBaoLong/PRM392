package com.prm392;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("login")  // Replace "login" with your actual endpoint path, e.g., "auth/login"
    Call<LoginActivity.AuthModels.LoginResponse> login(@Body LoginActivity.AuthModels.LoginRequest request);
}
