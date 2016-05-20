package com.doglandia.hometheater.introduction.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v17.leanback.app.OnboardingFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.doglandia.hometheater.R;
import com.doglandia.hometheater.introduction.fragment.intro.ApplicationInformationFragment;
import com.doglandia.hometheater.introduction.fragment.intro.DownloadClientFragment;
import com.doglandia.hometheater.introduction.fragment.intro.SelectVideoLocationsFragment;

/**
 * Created by tdk10 on 2/27/2016.
 */
public class IntroductionFragment extends OnboardingFragment {

    private static final String TAG = "IntroFragment";

    public interface OnOnboardingFinishedListener{
        void onOnboardingFinished();
    }

    private static final long NEXT = 1;
    private static final long MORE_INFO = 2;

    private OnOnboardingFinishedListener listener;

    private int foregroundContent = R.id.introduction_content;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public int onProvideTheme() {
        return R.style.Theme_Leanback_Onboarding;
    }

    @Override
    protected void onPageChanged(int newPage, int previousPage) {
        Log.d(TAG, "new page = "+newPage);
        // todo change fragment
        Fragment fragment = null;
        switch (newPage){
            case 0:
                fragment = new ApplicationInformationFragment();
                break;
            case 1:
                fragment = new DownloadClientFragment();
                break;
            case 2:
                fragment = new SelectVideoLocationsFragment();
        }
        if(fragment != null){
            getFragmentManager().beginTransaction()
                    .replace(foregroundContent,fragment)
//                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                    .commit();
        }
    }

    @Override
    protected int getPageCount() {
        return 3;
    }

    @Override
    protected String getPageTitle(int pageIndex) {
        return "";
    }

    @Override
    protected String getPageDescription(int pageIndex) {
        return "";
    }

    @Nullable
    @Override
    protected View onCreateBackgroundView(LayoutInflater inflater, ViewGroup container) {

        return null;
    }

    @Nullable
    @Override
    protected View onCreateContentView(LayoutInflater inflater, ViewGroup container) {
        Log.d(TAG, "created content");
        View view = inflater.inflate(R.layout.fragment_introduction, container, false);

        onPageChanged(0,-1);

        return view;
    }

    @Nullable
    @Override
    protected View onCreateForegroundView(LayoutInflater inflater, ViewGroup container) {

        return null;
    }

    @Override
    protected void onFinishFragment() {
        super.onFinishFragment();
        listener.onOnboardingFinished();

    }

    public void setOnboardingFinishedListener(OnOnboardingFinishedListener listener) {
        this.listener = listener;
    }

    //    @NonNull
//    @Override
//    public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
//        return new GuidanceStylist.Guidance("Get Started","To stream videos from your PC, download the PC app at doglandia.com/pc",
//                "",
//                getResources().getDrawable(R.drawable.app_icon_quantum));
//
//
//    }
//
//    @Override
//    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
//        super.onCreateActions(actions, savedInstanceState);
//
//        GuidedAction.Builder builder = new GuidedAction.Builder(getActivity());
//        builder.title("I've Done This");
//        builder.clickAction(1);
//        actions.add(builder.build());
//
//        builder = new GuidedAction.Builder(getActivity());
//        builder.title("More Information");
//        builder.clickAction(2);
//        actions.add(builder.build());
//    }
//
//    @Override
//    public void onGuidedActionClicked(GuidedAction action) {
//        super.onGuidedActionClicked(action);
//
//        if(action.getId() == NEXT){
//            ConnectingToPcFragment connectingToPcFragment = new ConnectingToPcFragment();
//            getFragmentManager().beginTransaction().add( R.id.intro_content, connectingToPcFragment, "connectingFragment").addToBackStack(null).commit();
//        }else if(action.getId() == MORE_INFO){
//
//        }
//    }
}
