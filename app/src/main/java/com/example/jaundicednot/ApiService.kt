import com.example.jaundicednot.ServerResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @Multipart
    @POST("predict")
    fun uploadImage(@Part image: MultipartBody.Part): Call<ServerResponse>
}
