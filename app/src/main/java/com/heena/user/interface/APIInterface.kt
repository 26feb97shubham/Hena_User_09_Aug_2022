package com.heena.user.`interface`

import android.text.TextUtils
import android.util.Log
import com.heena.user.`interface`.APIUtils.ServicePayment
import com.heena.user.`interface`.APIUtils.ServicePaymentTOKEN
import com.heena.user.`interface`.APIUtils.resultCodePayment
import com.heena.user.`interface`.APIUtils.resultExplanationPayment
import com.heena.user.`interface`.APIUtils.resultExplanationPaymentStatus
import com.heena.user.models.*
import okhttp3.*
import okhttp3.Response
import okio.Buffer
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface APIInterface {
    @POST(APIUtils.REGISTER)
    fun signUp(@Body body: RequestBody?): Call<SignUpResponse?>?

    @POST(APIUtils.LOGIN)
    fun login(@Body body: RequestBody?) : Call<LoginResponse?>?

    @GET(APIUtils.COUNTRIES)
    fun getCountries(@Path("lang") lang: String): Call<CountryListResponse?>?

    @GET(APIUtils.EMIRATES)
    fun getEmirates(@Path("lang") lang: String): Call<EmiratesListResponse?>?

    @POST(APIUtils.FORGOTPASSWORD)
    fun forgotPassword(@Body body: RequestBody?): Call<MyResponse?>?

    @POST(APIUtils.VERIFYOTP)
    fun verifyotp(@Body body: RequestBody?): Call<MyResponse?>?

    @POST(APIUtils.FORGOTPASSVERIFYOTP)
    fun forgotpassverifyotp(@Body body: RequestBody?): Call<MyResponse?>?

    @POST(APIUtils.RESETPASSWORD)
    fun resetpassword(@Body body: RequestBody?): Call<MyResponse?>?

    @POST(APIUtils.OTPRESEND)
    fun otpresend(@Body body: RequestBody?) : Call<OTPResend?>?

    @GET(APIUtils.LOGOUT)
    fun logout(@Path("user_id") user_id: Int, @Query("lang") lang : String)  :Call<LogoutResponse?>?

    @GET(APIUtils.getLoggedInUserProfile)
    fun getLoggedInUserProfile(@Path("user_id") user_id: Int, @Query("lang") lang : String) : Call<UserProfileResponse?>?

    @POST(APIUtils.CONTACTUS)
    fun contactUs(@Body body: RequestBody?) : Call<MyResponse?>?

    @GET(APIUtils.ADDRESSLISTING)
    fun getAddressList(@Path("user_id") user_id: Int, @Query("lang") lang : String) : Call<AddressListingResponse?>?

    @POST(APIUtils.ADDADDRESS)
    fun addAddress(@Body body: RequestBody?) : Call<MyResponse?>?

    @POST(APIUtils.EDITADDRESS)
    fun editAddress(@Body body: RequestBody?) : Call<MyResponse?>?


    @GET(APIUtils.SHOWADDRESS)
    fun showAddress(@Path("address_id") address_id: Int, @Query("lang") lang : String) : Call<ShowAddressResponse?>?

    @GET(APIUtils.DELETEADDRESS)
    fun deleteAddress(@Path("user_id") user_id: Int, @Query("lang") lang : String) : Call<MyResponse?>?

    @GET(APIUtils.DASHBOARDMANAGERSLISTING)
    fun dashboardManagersListing(@Query("lang") lang : String) : Call<ManagerListingResponse?>?

    @GET(APIUtils.MANAGERSLISTING)
    fun ManagersListing(@Query("lang") lang : String) : Call<ManagerListingResponse?>?

    @GET(APIUtils.CATEGORYLIST)
    fun categoryList(@Query("lang") lang : String) : Call<CategoryListResponse?>?

    @GET(APIUtils.SERVICESLISTING)
    fun servicesListing(@Path("user_id") user_id: Int,
                        @QueryMap query : HashMap<String, String>) : Call<ServicesListingResponse?>?

    @GET(APIUtils.SERVICESGUESTLISTING)
    fun servicesGuestListing(
        @QueryMap query : HashMap<String, String>) : Call<ServicesListingResponse?>?


    @GET(APIUtils.MANAGERPROFILE)
    fun getManagerProfile(@Path("manager_id") manager_id : Int,  @QueryMap query : HashMap<String, Any>) : Call<ManagerProfileResponse?>?

    @GET(APIUtils.GALLERYLISTING)
    fun getGallery(@Path("manager_id") manager_id: Int, @Query("lang") lang : String) : Call<GalleryResponse?>?

    @POST(APIUtils.ADDREVIEW)
    fun addReview(@Body body: RequestBody?) : Call<MyResponse?>?

    @POST(APIUtils.EDITPROFILE)
    fun editProfile(@Body body: RequestBody?) : Call<MyResponse?>?

    @POST(APIUtils.CHANGEPASSWORD)
    fun changePassword(@Body body: RequestBody?) : Call<MyResponse?>?

    @GET(APIUtils.COMMENTSLISTING)
    fun commentsListing(@Path("manager_id") manager_id : Int, @Query("lang") lang : String) :Call<ReviewsListingResponse?>?

    @POST(APIUtils.ADDFAVSERVICE)
    fun addServiceFav(@Body body: RequestBody?) : Call<MyResponse?>?

    @POST(APIUtils.DELFAVSERVICE)
    fun delServiceFav(@Body body: RequestBody?) : Call<MyResponse?>?

    @GET(APIUtils.FAVSERVICELISTING)
    fun getFavServices(@Path("user_id") user_id : Int, @Query("lang") lang : String) : Call<FavoriteServicesListingResponse?>?

    @POST(APIUtils.ADDFAVMANAGER)
    fun addManagerFav(@Body body: RequestBody?) : Call<MyResponse?>?

    @POST(APIUtils.DELFAVMANAGER)
    fun delManagerFav(@Body body: RequestBody?) : Call<MyResponse?>?

    @GET(APIUtils.FAVMANAGERLISTING)
    fun getFavManager(@Path("user_id") user_id : Int, @Query("lang") lang : String) : Call<FavManagerListingResponse?>?

    @GET(APIUtils.SHOWSERVICE)
    fun showService(@Path("service_id") service_id : Int, @Query("lang") lang : String) : Call<ServiceShowResponse?>?

    @GET(APIUtils.BOOKINGLISTING)
    fun bookingListing(@QueryMap query: HashMap<String, String>) : Call<BookingListingResponse?>?

    @GET(APIUtils.BOOKINGDETAILS)
    fun bookingDetails(@Path("booking_id") booking_id : Int, @Query("lang") lang : String) : Call<BookingDetailsResponse?>?

    @POST(APIUtils.BOOKINGCANCEL)
    fun bookingCancel(@Body body: RequestBody?) : Call<MyResponse?>?

    @GET(APIUtils.MANAGERDETAILS)
    fun getManagerDetails(@Path("manager_id") manager_id : Int, @Query("user_id") user_id : Int, @Query("lang") lang : String) : Call<ManagerDetailsResponse?>?

    @POST(APIUtils.ADDDELETECARD)
    fun addDeleteCard(@Body body: RequestBody?) : Call<MyResponse?>?

    @GET(APIUtils.SHOWCARDS)
    fun showCards(@Path("user_id") user_id  :Int, @Query("lang") lang : String) : Call<ViewCardResponse?>?

    @POST(APIUtils.SHOWOFFERS)
    fun showOffers(@Body body: RequestBody?) : Call<OffersResponse?>?

    @POST(APIUtils.DASH_HELP_CATEGORY)
    fun dashHelpCategory(@Body body: RequestBody?) : Call<DashHelpCategoryResponse?>?

    @POST(APIUtils.GET_OLD_MESSAGE_LIST)
    fun getOldMessageList(@Body body: RequestBody?) : Call<OldMessagesResponse?>?

    @POST(APIUtils.CHAT_FILE_UPLOAD)
    fun chatFileUpload(@Body body: RequestBody?) : Call<ChatFileUploadResponse?>?

    @POST(APIUtils.CREATECHARGE)
    fun create_charge(@Body body: RequestBody?, @Query("lang") lang : String) : Call<ResponseBody?>

    @POST(APIUtils.BOOKING_REDIRECT)
    fun booking_redirect(@Body body: RequestBody?) : Call<ResponseBody?>

    @POST(APIUtils.BOOKING_RESCHEDULE)
    fun booking_reschedule(@Body body: RequestBody?) : Call<ResponseBody?>

    @POST(APIUtils.PAYMENTTOKEN)
    fun paymentToken(@Body body: RequestBody?) : Call<ResponseBody?>

    @POST(APIUtils.successtransaction)
    fun successtransaction(@Body body: RequestBody?) : Call<ResponseBody?>

    @GET(APIUtils.NOTIFICATION)
    fun getNotification(@Path("user_id") user_id  :Int,  @Query("lang") lang : String) : Call<NotificationResponse?>?

    companion object {
        private var retrofit: Retrofit? = null
        private var paymentDPORetrofit: Retrofit? = null
        private var managerRetrofit: Retrofit? = null
        private val baseUrl: String = APIUtils.BASE_URL
        private val managerBaseUrl: String = APIUtils.MANAGER_BASE_URL
        fun getClient(): Retrofit? {
            if (retrofit == null) {
                val okHttpClient = OkHttpClient().newBuilder().connectTimeout(80, TimeUnit.SECONDS)
                    .readTimeout(80, TimeUnit.SECONDS).writeTimeout(80, TimeUnit.SECONDS)
                    .addInterceptor(LoginInterceptor()).build()

                retrofit = Retrofit.Builder().baseUrl(baseUrl).client(okHttpClient).addConverterFactory(
                    GsonConverterFactory.create()).build()
            }
            return retrofit
        }


        fun getDPOPaymentClient(): Retrofit? {
            if (paymentDPORetrofit == null) {
                val okHttpClient = OkHttpClient().newBuilder().connectTimeout(80, TimeUnit.SECONDS)
                    .readTimeout(80, TimeUnit.SECONDS).writeTimeout(80, TimeUnit.SECONDS)
                    .addInterceptor(PaymentLoginInterceptor()).build()

                paymentDPORetrofit = Retrofit.Builder().baseUrl(baseUrl).client(okHttpClient).addConverterFactory(
                    GsonConverterFactory.create()).build()
            }
            return paymentDPORetrofit
        }

        fun getManagerClient(): Retrofit? {
            if (managerRetrofit == null) {
                val okHttpClient = OkHttpClient().newBuilder().connectTimeout(80, TimeUnit.SECONDS)
                    .readTimeout(80, TimeUnit.SECONDS).writeTimeout(80, TimeUnit.SECONDS)
                    .addInterceptor(LoginInterceptor()).build()

                managerRetrofit = Retrofit.Builder().baseUrl(managerBaseUrl).client(okHttpClient).addConverterFactory(
                    GsonConverterFactory.create()).build()
            }
            return managerRetrofit
        }
        fun createBuilder(paramsName: Array<String>, paramsValue: Array<String>): FormBody.Builder {
            val builder = FormBody.Builder()
            for (i in paramsName.indices) {
                Log.e("create_builder:", paramsName[i] + ":" + paramsValue[i])
                if (!TextUtils.isEmpty(paramsValue[i])) {
                    builder.add(paramsName[i], paramsValue[i])
                } else {
                    builder.add(paramsName[i], "")
                }
            }
            return builder
        }
        fun createMultipartBodyBuilder(paramsName: Array<String>, paramsValue: Array<String>): MultipartBody.Builder? {
            val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
            for (i in paramsName.indices) {
                Log.e("multipart_builder:", paramsName[i] + ":" + paramsValue[i])
                if (!TextUtils.isEmpty(paramsValue[i])) {
                    builder.addFormDataPart(paramsName[i], paramsValue[i])
                } else {
                    builder.addFormDataPart(paramsName[i], "")
                }
            }
            return builder
        }

        class LoginInterceptor : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                val request = chain.request()
                val t1 = System.nanoTime()
                Log.e("OkHttp", String.format("--> Sending request %s on %s%n%s", request.url(), chain.connection(), request.headers()))
                try {
                    val requestBuffer = Buffer()
                    Log.e("OkHttp", requestBuffer.readUtf8().replace("=", ":").replace("&", "\n"))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                val response = chain.proceed(request)
                val t2 = System.nanoTime()
                Log.e("OkHttp", String.format("<-- Received response for %s in %.1fms%n%s", response.request().url(), (t2 - t1) / 1e6, response.headers()))
                val contentType = response.body()!!.contentType()
                val content = response.body()!!.string()
                Log.e("OkHttp", content)
               /* if (APIUtils.ServicePayment){
                    ServicePaymentTOKEN = content.split("<TransToken>")[1].split("</TransToken>")[0]
                }*/
                val wrappedBody = ResponseBody.create(contentType, content)
                return response.newBuilder().body(wrappedBody).build()
            }
        }
    }



    class PaymentLoginInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val t1 = System.nanoTime()
            Log.e("OkHttp", String.format("--> Sending request %s on %s%n%s", request.url(), chain.connection(), request.headers()))
            try {
                val requestBuffer = Buffer()
                Log.e("OkHttp", requestBuffer.readUtf8().replace("=", ":").replace("&", "\n"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val response = chain.proceed(request)
            val t2 = System.nanoTime()
            Log.e("OkHttp", String.format("<-- Received response for %s in %.1fms%n%s", response.request().url(), (t2 - t1) / 1e6, response.headers()))
            val contentType = response.body()!!.contentType()
            val content = response.body()!!.string()
            Log.e("OkHttp", content)

            resultCodePayment = content.split("<Result>")[1].split("</Result>")[0]
            resultExplanationPayment = content.split("<Result>")[1].split("</Result>")[1].split("<ResultExplanation>")[1].split("</ResultExplanation>")[0]
            if (resultCodePayment.equals("000")){
                if (APIUtils.ServicePayment){
                    resultExplanationPaymentStatus = true
                    ServicePaymentTOKEN = content.split("<TransToken>")[1].split("</TransToken>")[0]
                    ServicePayment = false
                    val wrappedBody = ResponseBody.create(contentType, content)
                    return response.newBuilder().body(wrappedBody).build()
                }else{
                    resultExplanationPaymentStatus = false
                    val wrappedBody = ResponseBody.create(contentType, content)
                    return response.newBuilder().body(wrappedBody).build()
                }
            }else{
                resultExplanationPaymentStatus = false
                val wrappedBody = ResponseBody.create(contentType, content)
                return response.newBuilder().body(wrappedBody).build()
            }
        }
    }
}