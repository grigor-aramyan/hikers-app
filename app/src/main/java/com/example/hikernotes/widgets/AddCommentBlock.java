package com.example.hikernotes.widgets;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.hikernotes.R;
import com.example.hikernotes.activities.DetailsActivity;
import com.example.hikernotes.consumptions.VolleyRequests;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by John on 8/18/2016.
 */
public class AddCommentBlock extends FrameLayout {
    private OnClickListener mClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.react_button_id:
                    ShowDialogAndGetName();
                    break;
                default:
                    break;
            }
        }
    };
    private OnCommentAdded mCommentAdded;
    private String mName = null;
    private EditText  comment_edt;
    private Button react_btn;
    private RequestQueue mQueue;
    private int mSelectedTourId;


    public AddCommentBlock(Context context) {
        super(context);
        this.mSelectedTourId = DetailsActivity.mSelectedTourID;
        mQueue = Volley.newRequestQueue(context);
        initViews();
    }

    public AddCommentBlock(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mSelectedTourId = DetailsActivity.mSelectedTourID;
        mQueue = Volley.newRequestQueue(context);
        initViews();
    }


    public void ShowDialogAndGetName(){


        LayoutInflater li = LayoutInflater.from(getContext());
        View promptsView = li.inflate(R.layout.edit_your_name_dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                getContext());

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.editTextDialogUserInput);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // get user input and set it to result
                                // edit text

                                if (!userInput.getText().toString().isEmpty()) {
                                    uploadComment(userInput.getText().toString());
                                }

                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();


    }

    public void uploadComment(final String namearg){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, VolleyRequests.sUrlForConnectivityCheck, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response.startsWith("got it")) {
                    String name, comment;
                    name = namearg;
                    comment = comment_edt.getText().toString();
                    if (name.isEmpty() || comment.isEmpty()) {
                        Toast.makeText(getContext(), "Can't make a comment w/ empty fields, pal. Input something!!!", Toast.LENGTH_LONG).show();
                        return;
                    }
                    final String data_encoded = mSelectedTourId + "YYY" + name + "YYY" + comment;
                    StringRequest stringRequest1 = new StringRequest(Request.Method.POST, VolleyRequests.sUrlForNewComment, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if (response.startsWith("ok")) {
                                Toast.makeText(getContext(), "We add new comment to this tour!! Update to see it!!", Toast.LENGTH_LONG).show();
                                comment_edt.setText("");

                                mCommentAdded.updateCommentsList();
                            }
                            if (response.startsWith("Connection"))
                                Toast.makeText(getContext(), "Connection issue! Try later, please!!", Toast.LENGTH_LONG).show();
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
    }


    private void initViews() {
        LayoutInflater.from(getContext()).inflate(R.layout.block_add_comment, this, true);
        comment_edt = (EditText) findViewById(R.id.comment_field_id);
        react_btn = (Button) findViewById(R.id.react_button_id);
        react_btn.setOnClickListener(mClickListener);
    }


    public interface OnCommentAdded {
        void updateCommentsList();
    }

    public void setOnCommentAdded(OnCommentAdded onCommentAdded) {
        mCommentAdded = onCommentAdded;
    }

}
