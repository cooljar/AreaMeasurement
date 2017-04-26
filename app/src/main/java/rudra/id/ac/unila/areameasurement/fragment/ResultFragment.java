package rudra.id.ac.unila.areameasurement.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rudra.id.ac.unila.areameasurement.R;

public class ResultFragment extends DialogFragment {
    private static final String ARG_PARAM_LUAS = "paramLuas";
    private static final String ARG_PARAM_DETAIL = "paramDetail";

    private String paramLuas;
    private ArrayList<String> paramDetail;

    @BindView(R.id.TvLuas) TextView TvLuas;
    @BindView(R.id.tvDetail) HtmlTextView tvDetail;
    @BindView(R.id.btTutup) Button btTutup;

    public ResultFragment() {
        // Required empty public constructor
    }

    public static ResultFragment newInstance(String paramLuas, ArrayList<String> paramDetail) {
        ResultFragment fragment = new ResultFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM_LUAS, paramLuas);
        args.putStringArrayList(ARG_PARAM_DETAIL, paramDetail);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            paramLuas = getArguments().getString(ARG_PARAM_LUAS);
            paramDetail = getArguments().getStringArrayList(ARG_PARAM_DETAIL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_result, container, false);
        ButterKnife.bind(this, view);

        TvLuas.setText("Luas area = " + paramLuas);
        tvDetail.setHtml(getDetailContent());
        btTutup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ResultFragment.this.dismiss();
            }
        });
        this.getDialog().setTitle("Hasil Penghitungan");
        return view;
    }

    private String getDetailContent(){
        StringBuilder sb = new StringBuilder();
        sb.append("<ul>");
        for(String ctn : paramDetail){
            sb.append("<li>"+ctn+"</li>");
        }
        sb.append("</ul>");
        return sb.toString();
    }
}
