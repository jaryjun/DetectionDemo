package com.compilesense.liuyi.detectiondemo.activity;

import android.app.ActionBar;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.compilesense.liuyi.detectiondemo.R;
import com.compilesense.liuyi.detectiondemo.model.Group;
import com.compilesense.liuyi.detectiondemo.model.Person;
import com.compilesense.liuyi.detectiondemo.platform_interaction.ResponseListener;
import com.compilesense.liuyi.detectiondemo.platform_interaction.apis.APIManager;
import com.compilesense.liuyi.detectiondemo.platform_interaction.RecognitionResponse;
import com.compilesense.liuyi.detectiondemo.utils.SpaceItemDecoration;
import com.compilesense.liuyi.detectiondemo.utils.Util;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GroupManageActivity extends BaseActivity {
    private final String TAG = "GroupManageActivity";


    GroupRecViewAdapter adapter;
    TextView name,tag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_manage);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setNavigationOnClickListener(new Toolbar.OnClickListener(){
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        initView();
        showDialog(this,"");
        fetchGroup();
    }



    void initView(){

        name = (TextView) findViewById(R.id.add_group_name);
        tag = (TextView) findViewById(R.id.add_group_tag);
        findViewById(R.id.create_group).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createGroup();
            }
        });
        initRecycleView();
    }

    void initRecycleView(){
        adapter = new GroupRecViewAdapter();
        adapter.listener = new ItemClickListener() {
            @Override
            public void onDeleteGroup(int position) {
                deleteGroup(adapter.groupList.get(position).group_id);
            }


            @Override
            public void onManagePerson(int position) {
                Group group = adapter.groupList.get(position);
                manageGroup(group.group_id,group.group_name);
            }

            @Override
            public void onRecognizeGroup(final int position) {
                getImage(new GetImageListener() {
                    @Override
                    public void getImage(Uri imageUri, Bitmap bitmap) {
                        Group g = adapter.groupList.get(position);
                        if (imageUri != null){
                            showDialog(GroupManageActivity.this,getResources().getString(R.string.recognize_face_ing));
                            APIManager.getInstance().recognizeImageGroup(GroupManageActivity.this,
                                    imageUri,
                                    g.group_id,
                                    new ResponseListener() {
                                        @Override
                                        public void success(String response) {
                                            dismissDialog();
                                            handRecognitionResponse(response);
                                        }

                                        @Override
                                        public void failed() {
                                            dismissDialog();
                                            Toast.makeText(GroupManageActivity.this,
                                                    getResources().getString(R.string.network_fail),
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        }else if (bitmap != null){
                            showDialog(GroupManageActivity.this,getResources().getString(R.string.recognize_face_ing));
                            APIManager.getInstance().recognizeImageGroup(GroupManageActivity.this,
                                    bitmap,
                                    g.group_id,
                                    new ResponseListener() {
                                        @Override
                                        public void success(String response) {
                                            dismissDialog();
                                            handRecognitionResponse(response);
                                        }

                                        @Override
                                        public void failed() {
                                            dismissDialog();
                                            Toast.makeText(GroupManageActivity.this,
                                                    getResources().getString(R.string.network_fail),
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }else {
                            Toast.makeText(GroupManageActivity.this,
                                    "请选择图片",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onChageInfo(int position) {
                final Group group = adapter.groupList.get(position);

                //获取一个group的信息
                APIManager.getInstance().getGroupInfo(GroupManageActivity.this,group.group_id  ,  new ResponseListener() {
                    @Override
                    public void success(String response) {

                        response = Util.string2jsonString(response);
                        Gson gson = new Gson();
                        try {
                            GroupInfoBean groupInfo= gson.fromJson(response, GroupInfoBean.class);
                            if (groupInfo.status.equals("OK")) {
                                String title=groupInfo.group_name+"     "+groupInfo.tag;
                                Util.buildEditDialog(GroupManageActivity.this,title,"名称","标签",new Util.DialogOnClickListener(){

                                    @Override
                                    public void onClick(int which) {}

                                    @Override
                                    public void onPosiButtonClick(int which, String text1, String text2) {
                                        showDialog(GroupManageActivity.this, "");

                                        APIManager.getInstance().updataGroup(GroupManageActivity.this,group.group_id  , text1, text2, new ResponseListener() {
                                            @Override
                                            public void success(String response) {
                                                dismissDialog();
                                                fetchGroup();
                                            }

                                            @Override
                                            public void failed() {
                                                dismissDialog();
                                                Toast.makeText(GroupManageActivity.this,
                                                        getResources().getString(R.string.network_fail),
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                });
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                            Toast.makeText(GroupManageActivity.this,getResources().getString(R.string.network_fail),Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void failed() {

                        Toast.makeText(GroupManageActivity.this,
                                getResources().getString(R.string.network_fail),
                                Toast.LENGTH_SHORT).show();
                    }
                });




            }
        };
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv_groups);
        recyclerView.setLayoutManager(new LinearLayoutManager(GroupManageActivity.this));
        recyclerView.addItemDecoration(new SpaceItemDecoration(16));
        recyclerView.setAdapter(adapter);
    }

    void createGroup(){
        String nameString = name.getText().toString();
        String tagString = tag.getText().toString();
        if (nameString == null || nameString.equals("")){
            Toast.makeText(this,"name不能为空",Toast.LENGTH_SHORT).show();
            return;
        }
        showDialog(this, "");
        APIManager.getInstance().createGroup(this,
                nameString,
                tagString,
                new ResponseListener() {
                    @Override
                    public void success(String response) {
                        dismissDialog();
                        name.setText("");
                        tag.setText("");

                        fetchGroup();
                    }

                    @Override
                    public void failed() {
                        dismissDialog();
                        Toast.makeText(GroupManageActivity.this,
                                getResources().getString(R.string.network_fail),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    void fetchGroup(){
        APIManager.getInstance().fetchGroup(this, new ResponseListener() {
            @Override
            public void success(String response) {
                response = Util.string2jsonString(response);
                Gson gson = new Gson();
                try {
                    ResponseGroupFetch responseGroupFetch = gson.fromJson(response, ResponseGroupFetch.class);
                    if (responseGroupFetch.status.equals("NO")){
                        responseGroupFetch.group = Collections.EMPTY_LIST;
                        Toast.makeText(GroupManageActivity.this,
                                "该账号下没有人员组数据，请添加",
                                Toast.LENGTH_SHORT).show();
                    }

                    adapter.setGroupList( responseGroupFetch.group );

                }catch (Exception e){
                    e.printStackTrace();
                }

                dismissDialog();

            }

            @Override
            public void failed() {
                Toast.makeText(GroupManageActivity.this,
                        getResources().getString(R.string.network_fail),
                        Toast.LENGTH_SHORT).show();
                dismissDialog();
            }
        });
    }

    void deleteGroup(String id){
        showDialog(this,"");
        APIManager.getInstance().deleteGroup(this, id, new ResponseListener() {
            @Override
            public void success(String response) {
                dismissDialog();
                fetchGroup();
            }

            @Override
            public void failed() {
                dismissDialog();
                Toast.makeText(GroupManageActivity.this,
                        getResources().getString(R.string.network_fail),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    void manageGroup(String id, String name){
        PersonManageActivity.startPersonManageActivity(this, id, name);
    }

    private void handRecognitionResponse(String response){
        response = Util.string2jsonString(response);
        Gson gson = new Gson();
        try{
            RecognitionResponse recognitionResponse = gson.fromJson(response,RecognitionResponse.class);

            if (recognitionResponse.Persons.size()<=0){
                Toast.makeText(this, "检测异常："+recognitionResponse.Exception,Toast.LENGTH_SHORT).show();
                return;
            }

            if (recognitionResponse.Persons.get(0).Passed){
                Toast.makeText(this,"识别通过",Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this,"识别未通过",Toast.LENGTH_SHORT).show();
            }

        }catch (Exception e){
            Toast.makeText(this,getResources().getString(R.string.network_fail),Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

    @Override
    void onDialogClick(int which) {

    }



    class ResponseGroupFetch{
        public String status;
        public List<Group> group;
    }

    public class GroupInfoBean{
        public String status;
        public String group_id;
        public String group_name;
        public String tag;
    }

    interface ItemClickListener{
        void onDeleteGroup(int position);
        void onManagePerson(int position);
        void onRecognizeGroup(int position);
        void onChageInfo(int position);
    }

    class GroupViewHolder extends RecyclerView.ViewHolder{
        View itemView;
        TextView itemName;
        View itemControl;
        Button deleteGroup, managePerson, recognizeGroup,changeInfo;

        public GroupViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            itemName = (TextView) itemView.findViewById(R.id.item_name);
            itemControl = itemView.findViewById(R.id.item_control);
            deleteGroup = (Button) itemView.findViewById(R.id.item_delete_group);
            managePerson = (Button) itemView.findViewById(R.id.item_manage_person);
            recognizeGroup = (Button) itemView.findViewById(R.id.item_recognize_group);
            changeInfo = (Button) itemView.findViewById(R.id.item_chage_group_info);
            itemControl.setVisibility(View.GONE);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (View.GONE == itemControl.getVisibility()){
                        itemControl.setVisibility(View.VISIBLE);
                    }else if (View.VISIBLE == itemControl.getVisibility()){
                        itemControl.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    class GroupRecViewAdapter extends RecyclerView.Adapter<GroupViewHolder>{
        public ItemClickListener listener;
        public List<Group> groupList = new ArrayList<>();

        public void setGroupList(List<Group> groupList) {
            this.groupList = groupList;
            Log.d(TAG,"groupList.size:"+groupList.size());
            notifyDataSetChanged();
        }

        @Override
        public GroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_list,parent,false);
            return new GroupViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final GroupViewHolder holder, int position) {
            if (listener == null){
                return;
            }
            Group group = groupList.get(position);
            holder.itemName.setText(group.group_name);
            holder.deleteGroup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onDeleteGroup(holder.getAdapterPosition());
                }
            });
            holder.managePerson.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onManagePerson(holder.getAdapterPosition());
                }
            });
            holder.recognizeGroup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onRecognizeGroup(holder.getAdapterPosition());
                }
            });
            holder.changeInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onChageInfo(holder.getAdapterPosition());
                }
            });
        }

        @Override
        public int getItemCount() {
            if(groupList!=null){
                return groupList.size();
            }
            return 0;

        }
    }

}
