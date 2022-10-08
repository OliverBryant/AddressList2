package com.example.addresslist;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.example.addresslist.encrypt.AesUtil;
import com.example.addresslist.encrypt.EncrypException;
import com.example.addresslist.encrypt.GeneKeyPair;
import com.example.addresslist.encrypt.KeyBytes;
import com.example.addresslist.encrypt.RsaUtil;
import com.example.addresslist.encrypt.SHA256withRSA;
import com.example.addresslist.entity.SimContactor;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import net.sf.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    ListView lv_userList;
    ImageButton ib_add;
    ImageButton ib_find;
    ImageButton ib_menu;
    ImageButton ib_menu1;
    ImageButton ib_exit;
    SimpleAdapter adapter = null;
    RelativeLayout ll_search;
    EditText et_search;
    LinearLayout gv_button_menu;
    public static File file = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        System.out.println(firstGenerate());
        loadUserList();
        menuBarJump();

    }

    private void loadUserList() {

        lv_userList = (ListView) this.findViewById(R.id.lv_userlist);//初始化

        Integer count = GeneKeyPair.keyCount(file);
        System.out.println("1111111111=" + count);
        System.out.println(GeneKeyPair.readKey(file, "PUBLIC_KEY_AS"));
        System.out.println(GeneKeyPair.readKey(file, "PRIVATE_KEY"));
        System.out.println(GeneKeyPair.readKey(file, "PUBLIC_KEY_IDEA"));
        System.out.println(GeneKeyPair.readKey(file, "num"));

        AsyncHttpClient client = new AsyncHttpClient();
        List<SimContactor> list = new ArrayList<>();

        RequestParams params = new RequestParams();

        String num = null;

        num=GeneKeyPair.readKey(file,"num");
        params.put("num", num);

        client.get("http://linux.gleaming.cn:18080/showAll", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {

                    String json = new String(responseBody, "utf-8");
                    List<SimContactor> list1 = JSON.parseArray(json, SimContactor.class);
//                    System.out.println(list1.toString());

                    if (list1.size() == 0) {
                        Toast.makeText(MainActivity.this, "解析失败", Toast.LENGTH_SHORT).show();
                        System.out.println("解析失败");
                    } else {

                        for (SimContactor simContactor : list1) {
                            list.add(simContactor);
                        }

                        ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
                        for (int i = 0; i < list.size(); i++) {
                            SimContactor simContactor = list.get(i);
                            Map<String, Object> map = new HashMap<>();
                            map.put("_id", simContactor.getId());
                            map.put("imageId", R.drawable.t2);
                            map.put("name", simContactor.getName());
                            data.add(map);
                        }

                        adapter = new SimpleAdapter(MainActivity.this, data, R.layout.list_item, new String[]{"imageId", "name"}, new int[]{R.id.user_image, R.id.tv_showname});
                        lv_userList.setAdapter(adapter);

                    }
                } catch (UnsupportedEncodingException e) {
                    Toast.makeText(MainActivity.this, "出现异常", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(MainActivity.this, "请求失败", Toast.LENGTH_SHORT).show();

            }
        });

        lv_userList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                HashMap map = (HashMap) adapterView.getItemAtPosition(i);

                String id = String.valueOf(map.get("_id"));
                RequestParams params = new RequestParams();
                params.put("id", id);
                params.put("num", GeneKeyPair.readKey(file, "num"));
                AsyncHttpClient client = new AsyncHttpClient();
                client.get("http://linux.gleaming.cn:18080/research", params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        String json = null;
                        try {

                            json = new String(responseBody, "utf-8");
                            List<String> list3 = JSON.parseArray(json, String.class);
                            System.out.println("list3" + list3.toString());

                            String encrypteKey = list3.get(0);
                            String busData = list3.get(1);

                            String decryptKey = RsaUtil.decrypt(encrypteKey, GeneKeyPair.readKey(file, "PRIVATE_KEY"));
                            System.out.println("解密后的密钥：" + decryptKey);

                            String decryptBusData = AesUtil.decrypt(busData, decryptKey);

                            JsonObject respDataJsonObject = JsonParser.parseString(decryptBusData).getAsJsonObject();
                            System.out.println("解密后的数据：" + respDataJsonObject.toString());

                            String decryptPlainText = respDataJsonObject.get("plainText").getAsString();
                            String decryptSignInfo = respDataJsonObject.get("signInfo").getAsString();
                            boolean verifySign = SHA256withRSA.verifySign(GeneKeyPair.readKey(file, "PUBLIC_KEY_IDEA"), decryptPlainText, decryptSignInfo);
                            System.out.println("是否验签通过：" + verifySign);

                            JSONObject jsonObject = new JSONObject();
                            if (verifySign == true) {
                                jsonObject = JSONObject.fromObject(decryptPlainText);
                            }

                            HashMap map1 = new HashMap();
                            map1.put("_id", jsonObject.get("id"));
                            map1.put("name", jsonObject.get("name"));
                            map1.put("num", jsonObject.get("num"));
                            map1.put("mobilePhone", jsonObject.get("mobilePhone"));
                            map1.put("officePhone", jsonObject.get("officePhone"));
                            map1.put("familyPhone", jsonObject.get("familyPhone"));
                            map1.put("position", jsonObject.get("position"));
                            map1.put("company", jsonObject.get("company"));
                            map1.put("address", jsonObject.get("address"));
                            map1.put("zipCode", jsonObject.get("zipcode"));
                            map1.put("email", jsonObject.get("email"));
                            map1.put("remark", jsonObject.get("remark"));


                            Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                            intent.putExtra("usermap", map1);
                            startActivityForResult(intent, 3);
                            finish();

                        } catch (UnsupportedEncodingException | EncrypException e) {
                            Toast.makeText(MainActivity.this, "查看联系人出现异常", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();

                        }

                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Toast.makeText(MainActivity.this, "查看联系人请求失败", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

    }

    public void menuBarJump() {
        //初始化
        ib_add = this.findViewById(R.id.ib_add);
        ib_find = this.findViewById(R.id.ib_find);
        ib_menu = this.findViewById(R.id.ib_menu);
        ib_exit = this.findViewById(R.id.ib_exit);
        ib_menu1 = this.findViewById(R.id.ib_menu1);
        ll_search = this.findViewById(R.id.ll_search);
        et_search = this.findViewById(R.id.et_search);
        gv_button_menu = this.findViewById(R.id.gv_button_menu);

        ib_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //添加类
                Intent i = new Intent(MainActivity.this, AddNewActivity.class);
                //启动   0代表请求到添加界面
                startActivityForResult(i, 0);
                //关闭当前页面
                finish();
            }
        });

        ib_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        ib_find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (et_search.getText().length() != 0) {

                    RequestParams params = new RequestParams();
                    //将文本框中输入的信息传给数据库
                    params.put("part", et_search.getText());
                    params.put("num", GeneKeyPair.readKey(file, "num"));
                    AsyncHttpClient client = new AsyncHttpClient();
                    List<SimContactor> list = new ArrayList<>();
                    client.get("http://linux.gleaming.cn:18080/showSome", params, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            try {
                                String json = new String(responseBody, "utf-8");
                                List<SimContactor> list1 = JSON.parseArray(json, SimContactor.class);
                                System.out.println(list1.toString());
                                if (list1.size() == 0) {
                                    Toast.makeText(MainActivity.this, "没有相关联系人", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    for (SimContactor simContactor : list1) {
                                        list.add(simContactor);
                                    }

                                    ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
                                    for (int i = 0; i < list.size(); i++) {

                                        SimContactor simContactor = list.get(i);
                                        Map<String, Object> map = new HashMap<>();
                                        map.put("_id", simContactor.getId());
                                        map.put("imageId", R.drawable.t2);
                                        map.put("name", simContactor.getName());
                                        data.add(map);
                                    }
                                    //将data中每个联系人的imageId，name按照布局list_item的方式展现出来
                                    adapter = new SimpleAdapter(MainActivity.this, data,
                                            R.layout.list_item, new String[]{"imageId", "name"},
                                            new int[]{R.id.user_image, R.id.tv_showname});
                                    lv_userList.setAdapter(adapter);//添加适配器 按照某种特定的方式展现出来

                                }
                            } catch (Exception e) {
                                Toast.makeText(MainActivity.this, "查找联系人出现异常", Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Toast.makeText(MainActivity.this, "查找联系人请求失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(MainActivity.this, "没有相关联系人", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }


            }
        });

        //菜单栏中的菜单图标按钮点击事件
        ib_menu1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (gv_button_menu.getVisibility() == View.GONE) { //若菜单栏处于隐藏状态
                    gv_button_menu.setVisibility(View.VISIBLE);  //则调整为显示状态
                } else {
                    gv_button_menu.setVisibility(View.GONE); //否则调整为隐藏状态
                }

                if (ll_search.getVisibility() == View.GONE) {//若查找框处于隐藏状态
                    ll_search.setVisibility(View.VISIBLE);//则调整为显示状态
                } else {
                    ll_search.setVisibility(View.GONE);//否则调整为隐藏状态
                }

                if (ib_menu.getVisibility() == View.GONE) { //若菜单栏外的菜单图标按钮处于隐藏状态
                    ib_menu.setVisibility(View.VISIBLE);//则调整为显示状态
                } else {
                    ib_menu.setVisibility(View.GONE);//否则调整为隐藏状态
                }
            }
        });

        //菜单栏外的菜单图标按钮点击事件
        ib_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (gv_button_menu.getVisibility() == View.GONE) { //若菜单栏处于隐藏状态
                    gv_button_menu.setVisibility(View.VISIBLE);  //则调整为显示状态
                } else {
                    gv_button_menu.setVisibility(View.GONE); //否则调整为隐藏状态
                }

                if (ll_search.getVisibility() == View.GONE) {//若查找框处于隐藏状态
                    ll_search.setVisibility(View.VISIBLE);//则调整为显示状态
                } else {
                    ll_search.setVisibility(View.GONE);//否则调整为隐藏状态
                }

                if (ib_menu.getVisibility() == View.GONE) { //若菜单栏外的菜单图标按钮处于隐藏状态
                    ib_menu.setVisibility(View.VISIBLE);//则调整为显示状态
                } else {
                    ib_menu.setVisibility(View.GONE);//否则调整为隐藏状态
                }
            }
        });

    }

    private boolean firstGenerate() {
        file = new File(getFilesDir(), "key.properties");
        System.out.println(file.getAbsolutePath());
        if (!file.exists()) {
            try {
                file.createNewFile();
                System.out.println("keys文件 创建成功");
            } catch (IOException e) {
                System.out.println("keys文件 创建失败");
                e.printStackTrace();
            }
        } else {
            System.out.println("keys文件 已存在");
        }


        Integer count = GeneKeyPair.keyCount(file);
        System.out.println("00000000000000888=" + count);
        System.out.println(GeneKeyPair.readKey(file, "PUBLIC_KEY_AS"));
        System.out.println(GeneKeyPair.readKey(file, "PRIVATE_KEY"));
        if (count == 0) {
            GeneKeyPair.generateKeyPair(file);
            count = GeneKeyPair.keyCount(file);
        }
        if (count == 2) {
            System.out.println("111111111111111111111111111111111111111111111111");
            AsyncHttpClient client = new AsyncHttpClient();
            RequestParams params = new RequestParams();
            params.put("PUBLIC_KEY_AS", GeneKeyPair.readKey(file, "PUBLIC_KEY_AS"));
            client.get("http://linux.gleaming.cn:18080/getKey", params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    try {
                        String json = new String(responseBody, "utf-8");
                        List<String> list = JSON.parseArray(json, String.class);
                        System.out.println("22222222222222=" + list.get(0));
                        System.out.println("333333333333=" + list.get(1));
                        GeneKeyPair.writeKey(file, "PUBLIC_KEY_IDEA", list.get(0));
                        GeneKeyPair.writeKey(file, "num", list.get(1));
                        System.out.println("55555555555555555" + GeneKeyPair.readKey(file, "PUBLIC_KEY_AS"));
                        System.out.println("66666666666666666666" + GeneKeyPair.readKey(file, "PRIVATE_KEY"));
                        System.out.println(GeneKeyPair.readKey(file, "PUBLIC_KEY_IDEA"));
                        System.out.println(GeneKeyPair.readKey(file, "num"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                }
            });
        }
        count = GeneKeyPair.keyCount(file);
        if (count == 4) {
            return true;
        }
        return false;
    }

}