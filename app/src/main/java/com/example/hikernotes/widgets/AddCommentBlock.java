package com.example.hikernotes.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.hikernotes.MainActivity;
import com.example.hikernotes.R;
import com.example.hikernotes.activities.DetailsActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by John on 8/18/2016.
 */
public class AddCommentBlock extends FrameLayout {
    private EditText name_edt, comment_edt;
    private Button react_btn;
    private OnClickListener mClickListener;
    private RequestQueue mQueue;
    private int mSelectedTourId;

    public AddCommentBlock(Context context) {
        super(context);
        this.mSelectedTourId = DetailsActivity.mSelectedTourID;
        mQueue = Volley.newRequestQueue(context);
        initInterfaces();
        initViews();
    }

    public AddCommentBlock(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mSelectedTourId = DetailsActivity.mSelectedTourID;
        mQueue = Volley.newRequestQueue(context);
        initInterfaces();
        initViews();
    }

    private void initInterfaces() {
        mClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.react_button_id:
                        StringRequest stringRequest = new StringRequest(Request.Method.POST, MainActivity.sUrlForConnectivityCheck, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                if (response.startsWith("got it")) {
                                    String name, comment;
                                    name = name_edt.getText().toString();
                                    comment = comment_edt.getText().toString();
                                    if (name.isEmpty() || comment.isEmpty()) {
                                        Toast.makeText(getContext(), "Can't make a comment w/ empty fields, pal. Input something!!!", Toast.LENGTH_LONG).show();
                                        return;
                                    }
                                    final String data_encoded = mSelectedTourId + "YYY" + name + "YYY" + comment;
                                    StringRequest stringRequest1 = new StringRequest(Request.Method.POST, MainActivity.sUrlForNewComment, new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {
                                            if (response.startsWith("ok")) {
                                                Toast.makeText(getContext(), "We add new comment to this tour!!", Toast.LENGTH_LONG).show();
                                                name_edt.setText("");
                                                comment_edt.setText("");
                                            }
                                            if (response.startsWith("Connection"))
                                                Toast.makeText(getContext(), "con issue", Toast.LENGTH_LONG).show();
                                        }
                                    }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            Toast.makeText(getContext(), "Some error occurs! Trying to solve it, guys!!",Toast.LENGTH_LONG).show();
                                            return;
                                        }
                                    }) {
                                        @Override
                                        protected Map<String, String> getParams() throws AuthFailureError {
                                            Map<String, String> params = new HashMap<>();
                                            params.put("data", data_encoded);
                                            return params;
                                        }

                                        @Override
                                        public Map<String, String> getHeaders() throws AuthFailureError {
                                            Map<String, String> params = new HashMap<>();
                                            params.put("Content-Type", "application/x-www-form-urlencoded");
                                            return params;
                                        }
                                    };
                                    mQueue.add(stringRequest1);
                                } else {
                                    Toast.makeText(getContext(), "Some problems with our serv! Sorry, try later, please!!", Toast.LENGTH_LONG).show();
                                    return;
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(getContext(), "Unidentified error. Sorry for this guys. We're trying to make it better!!!", Toast.LENGTH_LONG).show();
                                return;
                            }
                        });
                        mQueue.add(stringRequest);
                        break;
                    default:
                        break;
                }
            }
        };
    }

    private void initViews() {
        LayoutInflater.from(getContext()).inflate(R.layout.block_add_comment, this, true);
        name_edt = (EditText) findViewById(R.id.name_field_id);
        comment_edt = (EditText) findViewById(R.id.comment_field_id);
        react_btn = (Button) findViewById(R.id.react_button_id);
        react_btn.setOnClickListener(mClickListener);
    }



}
