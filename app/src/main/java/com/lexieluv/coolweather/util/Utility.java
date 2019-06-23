package com.lexieluv.coolweather.util;


import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.lexieluv.coolweather.db.City;
import com.lexieluv.coolweather.db.County;
import com.lexieluv.coolweather.db.Province;
import com.lexieluv.coolweather.gson.Weather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 4.创建这个工具类来处理JSON格式的数据
 * 7.还添加了一个处理weather的json格式的数据
 */

public class Utility {
    /*
    解析和处理服务器返回的省级数据
     */

    public static boolean handleProvinResponse(String response){
        if(!TextUtils.isEmpty(response)){
            try {
                //1）首先根据url地址，创建jsonarray数据获取这些数据
                JSONArray allProvinces = new JSONArray(response);
                //2）然后遍历这个数组，把每一个值交给jsonobject对象
                for(int i = 0;i < allProvinces.length();i++){
                    JSONObject provinceObject = allProvinces.getJSONObject(i);
                    Log.d("=====2=====",provinceObject.toString());
                    //3）并在这里创建实体类对象，并把json对象获取的值赋给这个实体类对象
                    Province province = new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.save();//注意：这里的意思是将数据存储到数据库中！
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        //因为else，再return就需要再也一条return，所以直接不写else，不过这条语句表达的就是如果上面执行不成功就执行这一条。
        return false;
    }

    /*
    解析和处理服务器返回的市级数据
     */
    public static boolean handleCityResponse(String response,int provinceId){
        //先判断一下内容是否为空
        if(!TextUtils.isEmpty(response)){
            //后面步骤和上面一样
            try {
                JSONArray allCities = new JSONArray(response);
                for(int i = 0;i < allCities.length();i++){
                    JSONObject cityObject = allCities.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);//对哦！直接在方法中把这个写进去就行了嘛
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /*
    解析和处理服务器返回的县级数据
     */
    public static boolean handleCountyResponse(String response,int cityId){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray allCounties = new JSONArray(response);
                for(int i = 0;i < allCounties.length();i++){
                    JSONObject countyObject = allCounties.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(countyObject.getString("name"));
                    county.setWeatherId(countyObject.getString("weather_id"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /*
    将返回的JSON数据解析成Weather实体类
     */
    public static Weather handleWeatherResponse(String response){
        try {
            JSONObject jsonObject = new JSONObject(response);//注意这里先搞清楚数据是数组还是对象类型，再来传入内容进行处理
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent,Weather.class);//返回用gson解析过的对象给天气类
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
