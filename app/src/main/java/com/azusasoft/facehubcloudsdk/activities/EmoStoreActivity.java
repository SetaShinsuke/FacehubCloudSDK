package com.azusasoft.facehubcloudsdk.activities;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.azusasoft.facehubcloudsdk.R;
import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.views.viewUtils.FacehubActionbar;

import java.util.ArrayList;

/**
 * Created by SETA on 2016/3/23.
 */
public class EmoStoreActivity extends AppCompatActivity {
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emoticon_store);
        context = this;
        //通知栏颜色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.facehub_color,getTheme()));
        }else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            getWindow().setStatusBarColor(getResources().getColor(R.color.facehub_color));
        }

        FacehubActionbar actionbar = (FacehubActionbar) findViewById(R.id.actionbar);
        actionbar.showSettings();
        actionbar.setTitle("面馆表情");
        actionbar.setOnBackBtnClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        SectionAdapter adapter = new SectionAdapter(context);
        recyclerView.setAdapter(adapter);

    }
}

class Section {
    String name;
    ArrayList<Package> packages = new ArrayList<>();
}

class SectionAdapter extends RecyclerView.Adapter<SectionHolder>{
    private Context context;
    private LayoutInflater layoutInflater;


    public SectionAdapter(Context context){
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public SectionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = layoutInflater.inflate(R.layout.section_item,parent,false);
        SectionHolder holder = new SectionHolder(convertView);
        return holder;
    }

    @Override
    public void onBindViewHolder(SectionHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}

class SectionHolder extends RecyclerView.ViewHolder{

    public SectionHolder(View itemView) {
        super(itemView);
    }
}
