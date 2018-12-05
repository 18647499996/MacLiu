package com.limin.myapplication3.fragment.mine;

import android.os.Bundle;
import android.view.View;

import com.blankj.utilcode.util.ToastUtils;
import com.ihidea.multilinechooselib.MultiLineChooseLayout;
import com.limin.myapplication3.R;
import com.limin.myapplication3.activity.swipemenu.SwipeMenuRecyclerViewActivity;
import com.limin.myapplication3.activity.videoplay.VideoPlayActivity;
import com.limin.myapplication3.base.BaseFragment;
import com.limin.myapplication3.service.BackgroundService;
import com.limin.myapplication3.utils.TitleBuilder;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView;

import butterknife.BindView;

/**
 * Description
 *
 * @author Created by: Li_Min
 * Time:2018/8/29
 */
public class MineFragment extends BaseFragment implements MineContract.View {
    @BindView(R.id.fragment_layout_folw)
    MultiLineChooseLayout fragmentLayoutFolw;

    private MineContract.Presenter presenter;

    @Override
    protected int loadViewLayout() {
        return R.layout.fragment_mine;
    }

    @Override
    protected TitleBuilder initBuilerTitle(View view) {
        return new TitleBuilder(getActivity(), view).setMiddleTitleBgRes("我的", R.color.black, R.color.with);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        presenter = new MinePresenter(this);
        presenter.start();
    }

    @Override
    protected void onClickDoubleListener(View paramView) {

    }

    @Override
    protected void setListener() {

    }

    @Override
    public void setPresenter(MineContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void showErrorMessage(String msg) {
        ToastUtils.showShort(msg);
    }

    @Override
    public void showAddView(String[] stringArray) {
        fragmentLayoutFolw.setList(stringArray);
        fragmentLayoutFolw.setOnItemClickListener((position, text) -> {
            switch (position) {
                case 0:
                    ToastUtils.showShort(text);
                    BackgroundService.startService(getContext());
                    break;
                case 1:
                    ToastUtils.showShort(text);
                    SwipeMenuRecyclerViewActivity.startActivity(getActivity());
                    break;
                case 2:
                    ToastUtils.showShort(text);
                    VideoPlayActivity.startActivity(getActivity());
                    break;
                case 3:
                    break;
                case 4:
                    break;
                case 5:
                    break;
                default:
                    break;
            }
        });

    }
}
