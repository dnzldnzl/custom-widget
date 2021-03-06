package com.example.detailmodule.fragments;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.detailmodule.R;
import com.example.detailmodule.mplay.IMediaPbService;
import com.example.detailmodule.utils.HttpUtils;
import com.example.detailmodule.utils.MediaUtils;
import com.example.detailmodule.utils.ParamsUtil;
import com.example.detailmodule.views.DetailViewPanel;
import com.example.detailmodule.views.InterceptEventView;

import java.io.UnsupportedEncodingException;

public class DetailFragment extends BaseFragment implements BaseFragment.ExitFragmentListener{
    private String mDetailTitle="";
    private String mDetailDescription="";
    private String mDetailBitmapUrl="";
    private DetailViewPanel mDetailViewPanel = null;

    public DetailFragment() {
        super();
        this.mDetailTitle = ParamsUtil.DETAIL_TITLE;
        this.mDetailDescription = ParamsUtil.DETAIL_DESCRIPTION;
        this.mDetailBitmapUrl = ParamsUtil.DETAIL_BITMAP_URL;
    }

    @Override
    protected void preLoadingData() {
        if (null == HttpUtils.getFileFromUri(ParamsUtil.DETAIL_PANORAMA_URL)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    HttpUtils.downLoadFileFromUri(getContext(), ParamsUtil.DETAIL_PANORAMA_URL, null, null);
                }
            }).start();
        }
    }

    @Override
    public void removeFragment() {
        ViewGroup contentView = getActivity().getWindow().getDecorView().findViewById(android.R.id.content);
        if (ismScreenPortrait()) {
            contentView.removeView(mDetailViewPanel.getPortraitView());
        } else {
            contentView.removeView(mDetailViewPanel.getLandscapeView());
        }

        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        Fragment detail = getActivity().getSupportFragmentManager().findFragmentByTag(BaseFragment.DETAIL_TAG);

        if (null != detail) {
            fragmentTransaction.hide(detail);
            fragmentTransaction.remove(detail);
            fragmentTransaction.commit();
        }
    }

    @Override
    public void removeViews() {
        ViewGroup contentView = getActivity().getWindow().getDecorView().findViewById(android.R.id.content);
        if (ismScreenPortrait()) {
            contentView.removeView(mDetailViewPanel.getPortraitView());
        } else {
            contentView.removeView(mDetailViewPanel.getLandscapeView());
        }
    }

    @Override
    public View initPortraitView(ViewGroup view) {
        if (null == mDetailViewPanel) {
            mDetailViewPanel = new DetailViewPanel(getContext());
        }
        mDetailViewPanel.setInitData(mDetailTitle, mDetailDescription, mDetailBitmapUrl);
        mDetailViewPanel = mDetailViewPanel.getPortraitView();
        mDetailViewPanel.setExitFragmentListener(this);
        mDetailViewPanel.setDetailFragment(this);
        mDetailViewPanel.setParentId(view.getId());
        view.removeView(mDetailViewPanel);
        view.addView(mDetailViewPanel);
        return mDetailViewPanel;
    }

    @Override
    public View initLandscapeView(ViewGroup view) {
        if (null == mDetailViewPanel) {
            mDetailViewPanel = new DetailViewPanel(getContext());
        }
        mDetailViewPanel.setInitData(mDetailTitle, mDetailDescription, mDetailBitmapUrl);
        mDetailViewPanel= mDetailViewPanel.getLandscapeView();
        mDetailViewPanel.setExitFragmentListener(this);
        mDetailViewPanel.setDetailFragment(this);
        mDetailViewPanel.setParentId(view.getId());
        view.removeView(mDetailViewPanel);
        view.addView(mDetailViewPanel);
        return mDetailViewPanel;
    }

    @Override
    public void dispatchOrientation(Configuration newConfig) {
        if (null == mDetailViewPanel) {
            mDetailViewPanel = new DetailViewPanel(getContext());
        }
        mDetailViewPanel.setExitFragmentListener(this);
        mDetailViewPanel.setDetailFragment(this);
        mDetailViewPanel.orientationChanged(newConfig);
        ViewGroup contentView = getActivity().getWindow().getDecorView().findViewById(android.R.id.content);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            initPortraitView(contentView);
        } else {
            initLandscapeView(contentView);
        }
    }

    public void showPanoramaView() {
        if (null == HttpUtils.getFileFromUri(ParamsUtil.DETAIL_PANORAMA_URL)) {
            try {
                String msg = new String("全景图片不可用，请检查参数或等待下载完成!".getBytes(), "UTF-8");
                Toast toast = Toast.makeText(getContext(),msg,Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER,0,0);
                toast.show();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return;
        }
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        PanoramaFragment panoramaFragment = new PanoramaFragment();

        fragmentTransaction.add(panoramaFragment,BaseFragment.PANORAMA_TAG);
        fragmentTransaction.show(panoramaFragment);

        Fragment detail = getActivity().getSupportFragmentManager().findFragmentByTag(BaseFragment.DETAIL_TAG);
        if (null != detail) {
            fragmentTransaction.hide(detail);
            fragmentTransaction.remove(detail);
        }

        fragmentTransaction.commit();
    }
}
