package com.example.addresslist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.addresslist.encrypt.AesUtil;
import com.example.addresslist.encrypt.EncrypException;
import com.example.addresslist.encrypt.GeneKeyPair;
import com.example.addresslist.encrypt.KeyBytes;
import com.example.addresslist.encrypt.RsaUtil;
import com.example.addresslist.encrypt.SHA256withRSA;
import com.example.addresslist.entity.Contector;
import com.google.gson.JsonObject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

public class DetailActivity extends Activity {

    ImageButton btn_img;
    EditText et_name;
    EditText et_mobilePhone;
    EditText et_officePhone;
    EditText et_familyPhone;
    EditText et_position;
    EditText et_company;
    EditText et_address;
    EditText et_zipcode;
    EditText et_email;
    EditText et_remark;
    HashMap map = null;
    Button btn_modify;
    Button btn_delete;
    Button btn_return;
    boolean flag = false;
    File file = MainActivity.file;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);
        //变量初始化
        initWidget();
        //设置为不可编辑
        setEditTextToDisable();

        Intent intent = getIntent();
        map =(HashMap) intent.getSerializableExtra("usermap");
        //变量赋值
        displayData();

        btn_modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (flag) {
                    flag = false;
                    btn_modify.setText("修改");
                    setEditTextToDisable();//设置为可编辑
                    //发送请求保存到数据库
                    afterModification(Integer.parseInt(String.valueOf(map.get("_id"))));
                } else {
                    flag = true;
                    btn_modify.setText("保存");

                    setEditTextAble();

                }

            }
        });
        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String id = String.valueOf(map.get("_id"));
                //写一个delete的post
                AsyncHttpClient client = new AsyncHttpClient();
                RequestParams params = new RequestParams();
                params.put("id",id);
                params.put("num",GeneKeyPair.readKey(file,"num"));
                client.get("http://linux.gleaming.cn:18080/delete", params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Toast.makeText(DetailActivity.this,"删除成功",Toast.LENGTH_SHORT).show();
                        Intent intent1 = new Intent(DetailActivity.this,MainActivity.class);
                        startActivity(intent1);
                        finish();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                    }
                });

            }
        });
        btn_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(DetailActivity.this,MainActivity.class);
                startActivity(intent1);
                finish();
            }
        });


    }

    public void initWidget() {

        et_name = (EditText) this.findViewById(R.id.et_name);
        et_mobilePhone = (EditText) this.findViewById(R.id.et_mobilePhone);
        et_officePhone = (EditText) this.findViewById(R.id.et_officePhone);
        et_familyPhone = (EditText) this.findViewById(R.id.et_familyPhone);
        et_position = (EditText) this.findViewById(R.id.et_position);
        et_company = (EditText) this.findViewById(R.id.et_company);
        et_address = (EditText) this.findViewById(R.id.et_address);
        et_zipcode = (EditText) this.findViewById(R.id.et_zipcode);
        et_email = (EditText) this.findViewById(R.id.et_email);
        et_remark = (EditText) this.findViewById(R.id.et_remark);
        btn_img = (ImageButton) this.findViewById(R.id.btn_img);
        btn_delete = (Button) this.findViewById(R.id.btn_delete);
        btn_modify = (Button)this.findViewById(R.id.btn_modify);
        btn_return = (Button)this.findViewById(R.id.btn_return);
    }

    private void setEditTextToDisable() {
          et_name.setEnabled(false);
        et_mobilePhone.setEnabled(false);
        et_officePhone.setEnabled(false);
        et_familyPhone.setEnabled(false);
        et_position.setEnabled(false);
        et_company.setEnabled(false);
        et_address.setEnabled(false);
        et_zipcode.setEnabled(false);
        et_email.setEnabled(false);
        et_remark.setEnabled(false);
        btn_img.setEnabled(false);

    }

    private void afterModification(int id) {
        String name = et_name.getText().toString();
        if (name.equals("")) {
            Toast.makeText(DetailActivity.this, "姓名不能为空", Toast.LENGTH_LONG).show();
            return;
        }
        String mobilePhone = et_mobilePhone.getText().toString();
        String officePhone = et_officePhone.getText().toString();
        String familyPhone = et_familyPhone.getText().toString();
        String position = et_position.getText().toString();
        String company = et_company.getText().toString();
        String address = et_address.getText().toString();
        String zipcode = et_zipcode.getText().toString();
        String email = et_email.getText().toString();
        String remark = et_remark.getText().toString();
        String imageId = String.valueOf(btn_img.getId());

        Contector contector = new Contector();
        contector.setId(id);
        contector.setName(name);
        contector.setMobilePhone(mobilePhone);
        contector.setOfficePhone(officePhone);
        contector.setFamilyPhone(familyPhone);
        contector.setPosition(position);
        contector.setCompany(company);
        contector.setAddress(address);
        contector.setZipcode(zipcode);
        contector.setEmail(email);
        contector.setRemark(remark);

        //签名
        JsonObject busDataJson = new JsonObject();
        busDataJson.addProperty("plainText",contector.toString());
        String signInfo = null;
        try {
            signInfo = SHA256withRSA.sign(GeneKeyPair.readKey(file,"PRIVATE_KEY"),contector.toString());
        } catch (EncrypException e) {
            e.printStackTrace();
        }
        busDataJson.addProperty("signInfo",signInfo);
        System.out.println("签名信息：" + signInfo);

        //AES密钥生成
        String aesKey = AesUtil.getAESSecureKey();
        System.out.println("密钥明文：" + aesKey);

        //加密随机密钥
        String encryptedAesKey = null;
        try {
            encryptedAesKey = RsaUtil.encrypt(aesKey,GeneKeyPair.readKey(file,"PUBLIC_KEY_IDEA"));
        } catch (EncrypException e) {
            e.printStackTrace();
        }
        System.out.println("密钥密文：" + encryptedAesKey);

        //数据加密
        String encrypt = null;
        try {
            encrypt = AesUtil.encrypt(busDataJson.toString(),aesKey);
        } catch (EncrypException e) {
            e.printStackTrace();
        }
        System.out.println("加密数据：" + encrypt);

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("encryptKey",encryptedAesKey);
        params.put("busData",encrypt);
        params.put("num",GeneKeyPair.readKey(file,"num"));

        client.get("http://linux.gleaming.cn:18080/modify", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Toast.makeText(DetailActivity.this, "保存成功", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(DetailActivity.this, "保存失败", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setEditTextAble() {
        et_name.setEnabled(true);
        et_mobilePhone.setEnabled(true);
        et_officePhone.setEnabled(true);
        et_familyPhone.setEnabled(true);
        et_position.setEnabled(true);
        et_company.setEnabled(true);
        et_address.setEnabled(true);
        et_zipcode.setEnabled(true);
        et_email.setEnabled(true);
        et_remark.setEnabled(true);
        btn_img.setEnabled(true);

    }

    private void displayData() {
        String name = String.valueOf(map.get("name"));
        et_name.setText(name == "null" ? "" : name);
        String mobilePhone = String.valueOf(map.get("mobilePhone"));
        et_mobilePhone.setText(mobilePhone == "null" ? "" : mobilePhone);
        String officePhone = String.valueOf(map.get("officePhone"));
        et_officePhone.setText(officePhone == "null" ? "" : officePhone);
        String familyPhone = String.valueOf(map.get("familyPhone"));
        et_familyPhone.setText(familyPhone == "null" ? "" : familyPhone);
        String position = String.valueOf(map.get("position"));
        et_position.setText(position == "null" ? "" : position);
        String company = String.valueOf(map.get("company"));
        et_company.setText(company == "null" ? "" : company);
        String address = String.valueOf(map.get("address"));
        et_address.setText(address == "null" ? "" : address);
        String zipcode = String.valueOf(map.get("zipcode"));
        et_zipcode.setText(zipcode == "null" ? "" : zipcode);
        String email = String.valueOf(map.get("email"));
        et_email.setText(email == "null"? "" : email);
        String remark = String.valueOf(map.get("remark"));
        et_remark.setText(remark == "null"? "" : remark);
        btn_img.setImageResource(R.drawable.t1);
    }

}
