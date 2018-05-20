package si.dragonhack.dragonhack2018;

import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface ApiClient {

    String BASE_URL = "https://api.getsongbpm.com/search/";

    @GET
    Call<Object> getSongsBpm(@Url String url);




}


