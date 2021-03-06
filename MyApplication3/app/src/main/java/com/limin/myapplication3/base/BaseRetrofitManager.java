package com.limin.myapplication3.base;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.util.TimeUtils;

import com.blankj.utilcode.util.LogUtils;
import com.limin.myapplication3.interceptor.ReceivedCookiesInterceptor;
import com.limin.myapplication3.interfaces.CommunityService;
import com.limin.myapplication3.interfaces.JournalismService;
import com.limin.myapplication3.interfaces.UserService;
import com.limin.myapplication3.model.UserModel;
import com.limin.myapplication3.receive.RxBus;
import com.limin.myapplication3.utils.Constant;
import com.limin.myapplication3.utils.GsonUtils;
import com.limin.myapplication3.utils.UserManagerUtils;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Description：Retrofit管理器（Retrofit + RxJava + Okhttp）
 *
 * @author Created by: Li_Min
 * Time:2018/8/2
 */
public class BaseRetrofitManager {

    private UserService userService;
    private CommunityService communityService;
    private JournalismService journalismService;
    private static SparseArray<Retrofit> sRetrofitManager = new SparseArray<>();


    private static volatile BaseRetrofitManager instance = null;

    private BaseRetrofitManager() {
    }

    public static BaseRetrofitManager getInstance() {
        //single chcekout
        if (null == instance) {
            synchronized (BaseRetrofitManager.class) {
                // double checkout
                if (null == instance) {
                    instance = new BaseRetrofitManager();
                }
            }
        }
        return instance;
    }

    /**
     * 用户服务器地址接口
     *
     * @return UserService
     */
    public UserService baseService() {
        initRetrofit(BaseHttpUrl.MAIN_TYPE);
        return userService;
    }

    /**
     * 业务服务器地址接口
     *
     * @return CommunityService
     */
    public CommunityService getCommunityService() {
        initRetrofit(BaseHttpUrl.COMMUNITY_TYPE);
        return communityService;
    }

    /**
     * 业务服务器地址接口
     *
     * @return CommunityService
     */
    public JournalismService getJournalismService() {
        initRetrofit(BaseHttpUrl.JOURNALISM_TYPE);
        return journalismService;
    }

    /**
     * 初始化Retrofit管理器配置
     *
     * @param hostType 服务器连接类型
     */
    private void initRetrofit(int hostType) {
        Retrofit retrofit = sRetrofitManager.get(hostType);
        if (retrofit == null) {
            // 初始化Retrofit配置
            Retrofit mRetrofit = new Retrofit.Builder()
                    .baseUrl(getHostType(hostType))
                    .client(getOkHttpClient(hostType))
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .build();
            sRetrofitManager.put(hostType, mRetrofit);
            switch (hostType) {
                // 主服务器地址接口信息配置
                case BaseHttpUrl.MAIN_TYPE:
                    userService = mRetrofit.create(UserService.class);
                    break;
                case BaseHttpUrl.COMMUNITY_TYPE:
                    communityService = mRetrofit.create(CommunityService.class);
                    break;
                case BaseHttpUrl.JOURNALISM_TYPE:
                    journalismService = mRetrofit.create(JournalismService.class);
                    break;
                default:
                    break;
            }
        }

    }

    /**
     * 初始化okHttp配置
     *
     * @param hostType 服务器类型（1、用户 2、社区）
     * @return okHttp
     */
    private OkHttpClient getOkHttpClient(@BaseHttpUrl.isChekout int hostType) {
        // 初始化OkHttp配置
        OkHttpClient okHttpClient = null;
        switch (hostType) {
            case BaseHttpUrl.MAIN_TYPE:
                okHttpClient = new OkHttpClient.Builder()
                        .connectTimeout(20, TimeUnit.SECONDS)
                        .readTimeout(20, TimeUnit.SECONDS)
                        .writeTimeout(20, TimeUnit.SECONDS)
                        .addInterceptor(new LogInterceptor())
                        .addInterceptor(new CodeInterceptor())
                        .build();
                break;
            case BaseHttpUrl.COMMUNITY_TYPE:
                okHttpClient = new OkHttpClient.Builder()
                        .connectTimeout(20, TimeUnit.SECONDS)
                        .readTimeout(20, TimeUnit.SECONDS)
                        .writeTimeout(20, TimeUnit.SECONDS)
                        .addInterceptor(new ParameterInterceptor())
                        .addInterceptor(new LogInterceptor())
                        .addInterceptor(new CodeInterceptor())
                        .build();
                break;
            case BaseHttpUrl.JOURNALISM_TYPE:
                okHttpClient = new OkHttpClient.Builder()
                        .connectTimeout(20, TimeUnit.SECONDS)
                        .readTimeout(20, TimeUnit.SECONDS)
                        .writeTimeout(20, TimeUnit.SECONDS)
                        .addInterceptor(new LogInterceptor())
                        .addInterceptor(new ReceivedCookiesInterceptor())
                        .build();
                break;
            default:
                break;
        }
        return okHttpClient;
    }

    /**
     * 获取连接服务器地址
     *
     * @param hostType 服务器地址类型
     * @return hostUrl
     */
    private String getHostType(@BaseHttpUrl.isChekout int hostType) {
        String hostUrl;
        switch (hostType) {
            // 用户服务器地址
            case BaseHttpUrl.MAIN_TYPE:
                hostUrl = BaseHttpUrl.USER_SERVICE;
                break;
            case BaseHttpUrl.COMMUNITY_TYPE:
                // 业务服务器地址
                hostUrl = BaseHttpUrl.COMMUNITY_SERVICE;
                break;
            case BaseHttpUrl.JOURNALISM_TYPE:
                hostUrl = BaseHttpUrl.JOURNALISM_SERVICE;
                break;
            default:
                hostUrl = BaseHttpUrl.USER_SERVICE;
                break;

        }
        return hostUrl;
    }

    /**
     * Log日志拦截器
     */
    private static class LogInterceptor implements Interceptor {
        @Override
        public Response intercept(@NonNull Chain chain) throws IOException {
            Request request = chain.request();
            long t1 = System.nanoTime();
            String bodyStr = bodyToString(request);
            LogUtils.json(this.getClass().getName(), String.format("请求参数 %s: body=   %s", URLDecoder.decode(URLDecoder.decode(String.valueOf(request.url()), "utf-8"), "utf-8"), URLDecoder.decode(URLDecoder.decode(bodyStr, "utf-8"), "utf-8")));

            Response response = chain.proceed(request);
            long t2 = System.nanoTime();
            if (response.body() != null) {
                ResponseBody body = response.peekBody(1024 * 1024);
                LogUtils.json(this.getClass().getName(), String.format(Locale.getDefault(), "返回数据 %s in %.1fms%n   %s", URLDecoder.decode(URLDecoder.decode(String.valueOf(response.request().url()), "utf-8"), "utf-8"), (t2 - t1) / 1e6d, body.string()));
            } else {
                Log.i(this.getClass().getName(), "body null");
                LogUtils.e("服务器数据异常" + request.url());

            }
            return response;

        }

        /**
         * 字符串输出
         *
         * @param request 请求数据
         * @return buffer
         */
        private static String bodyToString(final Request request) {

            try {
                final Request copy = request.newBuilder().build();
                final Buffer buffer = new Buffer();
                if (copy == null || copy.body() == null) {
                    return "";
                }
                Objects.requireNonNull(copy.body()).writeTo(buffer);
                return buffer.readUtf8();
            } catch (final IOException e) {
                return "did not work";
            }
        }
    }

    /**
     * 服务器返回code编码拦截器
     * 根据需求处理统一拦截
     * 比如token异常处理 与服务器定义好统一code编码
     */
    public static class CodeInterceptor implements Interceptor {

        @Override
        public Response intercept(@NonNull Chain chain) throws IOException {
            Request request = chain.request();

            Response response = chain.proceed(request);
            ResponseBody body = response.peekBody(1024 * 1024);

            String bodyStr = body.string();
            int code;
            String msg;

            if (!TextUtils.isEmpty(bodyStr)) {
                BaseResult baseResult = GsonUtils.fromJson(bodyStr, BaseResult.class);
                if (null == baseResult) {
                    throw new BaseTransformer.ServerException();
                }
                code = baseResult.getCode();
                msg = baseResult.getMsg();
            } else {
                throw new BaseTransformer.ServerException();
            }

            switch (code) {
                case 0:
                    //code编码主要根据服务器返回识别
                    return response;
                case 200:
                    return response;
                case -7:
                    // 可以通过发送RxBus通知进入登录页面
                    RxBus.get().post(Constant.TOKEN);
                    throw new BaseTransformer.TokenException(code, msg);
                default:
                    throw new BaseTransformer.ServerException(code, msg);
            }
        }


    }

    /**
     * 参数拦截器
     */
    private class ParameterInterceptor implements Interceptor {

        @Override
        public Response intercept(@NonNull Chain chain) throws IOException {
            Request request = chain.request();
            UserModel userModel = UserManagerUtils.getInstance().getUserModel();
            String token = userModel.getToken();
            if (request.url().toString().startsWith(BaseHttpUrl.COMMUNITY_SERVICE)) {
                return chain.proceed(addCommunityParams(request, token));
            }
            return chain.proceed(request);
        }

        /**
         * 社区服务器参数拦截处理
         *
         * @param request 请求数据
         * @param token   token令牌
         * @return request
         */
        private Request addCommunityParams(Request request, String token) {
            // 将参数拼接到body里
            RequestBody builder = request.body();
            UserModel userModel = UserManagerUtils.getInstance().getUserModel();
            FormBody.Builder newBuilder = new FormBody.Builder();
            newBuilder.add("user", GsonUtils.toJson(userModel));
            newBuilder.add("token", token);

            if (builder instanceof FormBody) {
                FormBody formBody = (FormBody) builder;
                for (int i = 0; i < formBody.size(); i++) {
                    newBuilder.add(formBody.name(i), formBody.value(i));
                }
            }
            return request.newBuilder()
                    .method(request.method(), request.body())
                    .post(newBuilder.build())
                    .build();
        }
    }
}
