package pan.alexander.tordnscrypt.tor_fragment;

/*
    This file is part of InviZible Pro.

    InviZible Pro is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    InviZible Pro is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with InviZible Pro.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2019-2020 by Garmatin Oleksandr invizible.soft@gmail.com
*/

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import pan.alexander.tordnscrypt.MainActivity;
import pan.alexander.tordnscrypt.R;
import pan.alexander.tordnscrypt.TopFragment;
import pan.alexander.tordnscrypt.utils.RootExecService;

import static pan.alexander.tordnscrypt.TopFragment.TorVersion;
import static pan.alexander.tordnscrypt.utils.RootExecService.LOG_TAG;


public class TorRunFragment extends Fragment implements TorFragmentView, View.OnClickListener, ViewTreeObserver.OnScrollChangedListener {


    private Button btnTorStart;
    private TextView tvTorStatus;
    private ProgressBar pbTor;
    private TextView tvTorLog;
    private ScrollView svTorLog;
    private BroadcastReceiver receiver;

    private TorFragmentPresenter presenter;


    public TorRunFragment() {
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tor_run, container, false);

        btnTorStart = view.findViewById(R.id.btnTorStart);

        //Not required for a portrait orientation, so return
        if (btnTorStart == null) {
            return view;
        }

        btnTorStart.setOnClickListener(this);

        pbTor = view.findViewById(R.id.pbTor);

        tvTorLog = view.findViewById(R.id.tvTorLog);

        svTorLog = view.findViewById(R.id.svTorLog);
        svTorLog.getViewTreeObserver().addOnScrollChangedListener(this);

        tvTorStatus = view.findViewById(R.id.tvTorStatus);

        setTorLogViewText();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        //MainFragment do this job for portrait orientation, so return
        if (btnTorStart == null) {
            return;
        }

        presenter = new TorFragmentPresenter(this);

        receiver = new TorFragmentReceiver(this, presenter);

        if (getActivity() != null) {
            IntentFilter intentFilterBckgIntSer = new IntentFilter(RootExecService.COMMAND_RESULT);
            IntentFilter intentFilterTopFrg = new IntentFilter(TopFragment.TOP_BROADCAST);

            getActivity().registerReceiver(receiver, intentFilterBckgIntSer);
            getActivity().registerReceiver(receiver, intentFilterTopFrg);

            presenter.onStart(getActivity());
        }

    }

    @Override
    public void onStop() {
        super.onStop();

        try {
            if (getActivity() != null && receiver != null) {
                getActivity().unregisterReceiver(receiver);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "TorFragment onStop exception " + e.getMessage() + " " + e.getCause());
        }

        if (presenter != null) {
            presenter.onStop();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnTorStart) {
            presenter.startButtonOnClick(getActivity());
        }
    }

    @Override
    public void setTorStatus(int resourceText, int resourceColor) {
        tvTorStatus.setText(resourceText);
        tvTorStatus.setTextColor(getResources().getColor(resourceColor));
    }

    @Override
    public void setTorStatus(String text, int resourceColor) {
        tvTorStatus.setText(text);
        tvTorStatus.setTextColor(getResources().getColor(resourceColor));
    }

    @Override
    public void setTorStartButtonEnabled(boolean enabled) {
        if (btnTorStart.isEnabled() && !enabled) {
            btnTorStart.setEnabled(false);
        } else if (!btnTorStart.isEnabled() && enabled) {
            btnTorStart.setEnabled(true);
        }
    }

    @Override
    public void setStartButtonText(int textId) {
        btnTorStart.setText(textId);
    }

    @Override
    public void setTorProgressBarIndeterminate(boolean indeterminate) {
        if (!pbTor.isIndeterminate() && indeterminate) {
            pbTor.setIndeterminate(true);
        } else if (pbTor.isIndeterminate() && !indeterminate){
            pbTor.setIndeterminate(false);
        }
    }

    @Override
    public void setTorProgressBarProgress(int progress) {
        pbTor.setProgress(progress);
    }

    @Override
    @SuppressLint("SetTextI18n")
    public void setTorLogViewText() {
        tvTorLog.setText(getText(R.string.tvDNSDefaultLog) + " " + TorVersion);
    }

    @Override
    public void setTorLogViewText(Spanned text) {
        tvTorLog.setText(text);
    }

    @Override
    public Activity getFragmentActivity() {
        return getActivity();
    }

    @Override
    public FragmentManager getFragmentFragmentManager() {
        return getParentFragmentManager();
    }

    public TorFragmentPresenterCallbacks getPresenter() {
        if (presenter == null && getActivity() instanceof MainActivity && ((MainActivity)getActivity()).getMainFragment() != null) {
            presenter = ((MainActivity)getActivity()).getMainFragment().getTorFragmentPresenter();
        }

        return presenter;
    }

    @Override
    public void onScrollChanged() {
        if (presenter != null && svTorLog != null) {
            if (svTorLog.canScrollVertically(1) && svTorLog.canScrollVertically(-1)) {
                presenter.torLogAutoScrollingAllowed(false);
            } else {
                presenter.torLogAutoScrollingAllowed(true);
            }
        }
    }

    @Override
    public void scrollTorLogViewToBottom() {
        svTorLog.post(() -> {
            View lastChild = svTorLog.getChildAt(svTorLog.getChildCount() - 1);
            int bottom = lastChild.getBottom() + svTorLog.getPaddingBottom();
            int sy = svTorLog.getScrollY();
            int sh = svTorLog.getHeight();
            int delta = bottom - (sy + sh);

            svTorLog.smoothScrollBy(0, delta);
        });
    }
}
