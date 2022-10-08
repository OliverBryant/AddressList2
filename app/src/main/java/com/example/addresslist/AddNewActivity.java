package com.example.addresslist;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

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
import java.util.Map;

import cz.msebera.android.httpclient.Header;


public class AddNewActivity extends Activity {
    //声明变量
    ImageButton btn_img;
    AlertDialog imageChooseDialog;
    View view;
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
    Button btn_save;
    Button btn_return;
    File file = MainActivity.file;

    private int[] images = {R.drawable.t1, R.drawable.t2,
            R.drawable.t3, R.drawable.t4,
            R.drawable.t5, R.drawable.t6,
            R.drawable.t7, R.drawable.t8,
            R.drawable.t9, R.drawable.t10,
            R.drawable.t11, R.drawable.t12,
            R.drawable.t13, R.drawable.t14,
            R.drawable.t15, R.drawable.t16,
            R.drawable.t17, R.drawable.t18,
            R.drawable.t19, R.drawable.t20,
            R.drawable.t21, R.drawable.t22,
            R.drawable.t23, R.drawable.t24,
            R.drawable.t25};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addnew);
        //变量初始化
        initWidget();

        btn_save.setOnClickListener(new View.OnClickListener() {

            private Object Contector;

            @Override
            public void onClick(View view) {
                String name = et_name.getText().toString();
                if (name.equals("")) {
                    Toast.makeText(AddNewActivity.this, "姓名不能为空", Toast.LENGTH_LONG).show();
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

                Contector contector = new Contector();
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
                System.out.println(contector.toString());

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


                RequestParams params = new RequestParams();
                params.put("encryptKey",encryptedAesKey);
                params.put("busData",encrypt);
                params.put("num",GeneKeyPair.readKey(file,"num"));
                AsyncHttpClient client = new AsyncHttpClient();
                client.post("http://linux.gleaming.cn:18080/addnew", params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        try {
                            Toast.makeText(AddNewActivity.this,"添加成功",Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(AddNewActivity.this,MainActivity.class);
                            startActivity(intent);
                            AddNewActivity.this.finish();
                        } catch (Exception e){
                            e.printStackTrace();
                            Toast.makeText(AddNewActivity.this,"添加出现异常",Toast.LENGTH_SHORT).show();
                            AddNewActivity.this.finish();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Toast.makeText(AddNewActivity.this,"添加请求失败",Toast.LENGTH_SHORT).show();
                        AddNewActivity.this.finish();
                    }
                });

            }
        });

        btn_img = (ImageButton) this.findViewById(R.id.btn_img);

        btn_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              //  initImageChooseDialog();
                imageChooseDialog.show();

            }
        });

        btn_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddNewActivity.this,MainActivity.class);
                startActivity(intent);
                AddNewActivity.this.finish();
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

        btn_save = this.findViewById(R.id.btn_save);
        btn_return = this.findViewById(R.id.btn_return);
    }

}