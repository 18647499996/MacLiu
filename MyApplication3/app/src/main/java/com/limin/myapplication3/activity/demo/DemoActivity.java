package com.limin.myapplication3.activity.demo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.blankj.utilcode.util.ToastUtils;
import com.limin.myapplication3.R;
import com.limin.myapplication3.activity.test.TestActivity;
import com.limin.myapplication3.base.BaseActivity;
import com.limin.myapplication3.model.UserModel;
import com.limin.myapplication3.utils.TitleBuilder;

import butterknife.BindView;

/**
 * Description
 *
 * @author Created by: Li_Min
 * Time:2018/8/4
 */
public class DemoActivity extends BaseActivity implements DemoContract.View {

    @BindView(R.id.activity_demo_btn)
    Button activityDemoBtn;
    private DemoContract.Presenter mPresenter;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, DemoActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_demo;
    }

    @Override
    protected TitleBuilder initBuilerTitle() {
        return new TitleBuilder(this)
                .setMiddleTitleBgRes("Demo", R.color.with,R.color.colorAccent)
                .setLeftTextRes("返回",16,R.color.with)
                .setLeftRelativeLayoutListener(this);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        immersionBar.transparentStatusBar().statusBarDarkFont(false).init();
        mPresenter = (DemoContract.Presenter) new DemoPresenter(this).Bulider(this);
        mPresenter.start();
    }

    @Override
    protected void addListener() {
        activityDemoBtn.setOnClickListener(this);
    }

    @Override
    protected void onClickDoubleListener(View v) {
        switch (v.getId()) {
            case R.id.activity_demo_btn:
                mPresenter.demo();
                break;
            default:
                break;
        }
    }



    @Override
    public void setPresenter(DemoContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }

    @Override
    public void showErrorMessage(String msg) {
        ToastUtils.showShort(msg);
    }

    @Override
    public void showUserModel(UserModel userModel) {
        // 获取用户数据更新界面
        TestActivity.startActivity(this);
    }
}
