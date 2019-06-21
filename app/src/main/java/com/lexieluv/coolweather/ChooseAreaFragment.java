package com.lexieluv.coolweather;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lexieluv.coolweather.db.City;
import com.lexieluv.coolweather.db.County;
import com.lexieluv.coolweather.db.Province;
import com.lexieluv.coolweather.util.HttpUtil;
import com.lexieluv.coolweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 5.创建碎片，用于遍历省市县数据，《初始化工作》
 */
public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;
    private ProgressDialog progressDialog;//这里提前定义了一个进度条对话框
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();
    /*
    省列表
     */
    private List<Province> provinceList;
    /*
    市列表
     */
    private List<City> cityList;
    /*
    县列表
     */
    private List<County> countyList;
    /*
    选中的省份
     */
    private Province selectedProvince;
    /*
    选中的城市
     */
    private City selectedCity;
    /*
    当前选中的级别
     */
    private int currentLevel;

    @Nullable
    @Override//这个方法里面是绑定视图的，适配器绑定与设置也在这里
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area,container,false);
        titleText = view.findViewById(R.id.title_text);
        backButton = view.findViewById(R.id.back_button);
        listView = view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(getContext(),R.layout.support_simple_spinner_dropdown_item,dataList);
        return view;
    }

    @Override//这个方法里是写事件的,主要是写listview中的单个项目点击
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentLevel == LEVEL_PROVINCE){
                    selectedProvince = provinceList.get(position);
                    queryCities();//单独创立一个方法，下面有介绍
                } else if (currentLevel == LEVEL_CITY){
                    selectedCity = cityList.get(position);
                    queryCounties();
                }
            }
        });
        //这里让back按钮才判断是否是县级，因为县级是最低一级，返回去的时候就可以判断是否是城市还是省份
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentLevel == LEVEL_COUNTY){
                    queryCities();
                }else if (currentLevel == LEVEL_CITY){
                    queryCounties();
                }
            }
        });
        queryProvinces();//什么都不是的时候，当然是呆在最大的省份处啦！
    }
    /*
    查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器上查询
     */
    private void queryProvinces() {
       titleText.setText("中国");//这里涉及到很对UI操作，所以必须在主线程操作，所以下面调用这里的时候必须开启一个新的线程
       backButton.setVisibility(View.GONE);
       //1）看下数据库里面有没有
       provinceList = DataSupport.findAll(Province.class);//这里直接用litepal数据库自带的方法来查询
        if(provinceList.size() > 0){
            dataList.clear();
            for(Province province : provinceList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();//适配器的改变调整
            listView.setSelection(0);//这里应该是设置选择为0个listview项目
            currentLevel = LEVEL_PROVINCE;
        } else {//2）没有就在网上获取
            String address = "http://guolin.tech/api/china";
            queryFromServer(address,"province");//要创建一个在网上找的方法，逻辑规律地处理问题！！
        }
    }

    /*
        查询省内所有市，还是先找数据库，再看网上的
         */
    private void queryCities() {
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);//这里因为可以返回，所以按钮可见
        cityList = DataSupport.where("provinceid = ?",
                String.valueOf(selectedProvince.getId())).find(City.class);//这里不像上面找全部，而是按条件查找
        if(cityList.size() > 0){
            dataList.clear();
            for(City city : cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            //这里要先把身份的代码获取
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china" + provinceCode;
            queryFromServer(address,"city");
        }
    }

    /*
    查询县的，仍然先数据库，再网络
     */
    private void queryCounties() {
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityid = ?",
                String.valueOf(selectedCity.getId())).find(County.class);
        if(countyList.size() > 0){
            dataList.clear();
            for (County county : countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china" + provinceCode + "/"
                    + cityCode;
            queryFromServer(address,"county");
        }
    }

    /*
    最重要的一个：根据传入的地址和类型从服务器上查询省市县数据
     */
    private void queryFromServer(String address, final String type) {
        showProgressDialog();//还要创建一个对话框
        //下面这里用到了okhttp里面的方法，由于这个类已经被封装过，所以下面的操作会自然在子线程进行网络操作
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            //1）这个应该是成功响应的操作
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respronseText  = response.body().string();
                boolean result = false;//result是一个布尔类型的值
                if("province".equals(type)){
                    result = Utility.handleProvinResponse(respronseText);
                } else if ("city".equals(type)){
                    result = Utility.handleCityResponse(respronseText,selectedProvince.getId());
                } else if ("county".equals(type)){
                    result = Utility.handleCountyResponse(respronseText,selectedCity.getId());
                }
                if (result){//true的话就开启一个UI线程
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();//关闭对话框方法
                            if ("province".equals(type)){
                                queryProvinces();//这里是因为，如果有的话，就又返回判断是否有缓存那里，因为既然有就一定要先访问缓存！
                            } else if ("city".equals(type)){
                                queryCities();
                            } else if ("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }
            //2）这个应该是响应失败的操作
            @Override
            public void onFailure(Call call, IOException e) {
                //还是通过新开UI线程来回到主线程处理逻辑
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败！",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /*
    用来显示进度对话框
     */
    private void showProgressDialog() {
        if (progressDialog == null){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载。。。");
            progressDialog.setCanceledOnTouchOutside(false);//触碰对话框以外地区的设置事件
        }
        progressDialog.show();
    }
    /*
    用来关闭进度对话框
     */
    private void closeProgressDialog() {
        if (progressDialog != null){
            progressDialog.dismiss();
        }
    }
}
