package com.app.torbjornzetterlund.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.app.torbjornzetterlund.R;
import com.app.torbjornzetterlund.app.AppController;
import com.app.torbjornzetterlund.app.Const;
import com.app.torbjornzetterlund.utils.Utils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PostCommentsTab  extends Fragment {

    private static final String TAG = PostCommentsTab.class.getSimpleName();
    public String post_id;
    public Boolean user_can_comment;

    private ProgressDialog pDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =inflater.inflate(R.layout.tab_postcomments,container,false);

        pDialog = new ProgressDialog(getActivity());
        final EditText your_name = (EditText) v.findViewById(R.id.your_name);
        final EditText your_email = (EditText) v.findViewById(R.id.your_email);
        final EditText your_comment = (EditText) v.findViewById(R.id.your_comment);



        Button post_comment = (Button) v.findViewById(R.id.post_comment);
        post_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user_can_comment==true) {
                    Boolean onError = false;
                    final String email = your_email.getText().toString();
                    if (!isValidEmail(email)) {
                        onError = true;
                        your_email.setError(getString(R.string.error_invalid_email));
                    }

                    final String name = your_name.getText().toString();
                    if (!isValidString(name, Const.NameValidationLimit)) {
                        onError = true;
                        your_name.setError(String.format(getString(R.string.error_length), Integer.toString(Const.NameValidationLimit)));
                    }

                    final String comment = your_comment.getText().toString();
                    if (!isValidString(comment, Const.CommentsValidationLimit)) {
                        onError = true;
                        your_comment.setError(String.format(getString(R.string.error_length), Integer.toString(Const.CommentsValidationLimit)));
                    }
                    if (!onError) {

                        pDialog.setTitle(getString(R.string.comment_post_title));
                        pDialog.setMessage(getString(R.string.comment_post_description));
                        pDialog.show();

                        //Requesting The Story
                        String url = null;
                        url = Const.URL_COMMENTS_LIST_PAGE.replace("_STORY_ID_", post_id.replace("P", ""));

                        // making fresh volley request and getting json
                        StringRequest sr = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                pDialog.hide();
                                JsonParser jsonParser = new JsonParser();
                                JsonObject jo = (JsonObject)jsonParser.parse(response);

                                try {
                                    JSONObject obj = new JSONObject(response);
                                    if (obj.has("error")) {
                                        String error = obj.getString("error");
                                        Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
                                    } else {
                                        //parseJsonFeed(response);
                                        your_comment.setText("");
                                        your_name.setText("");
                                        your_email.setText("");
                                        Utils.showAlertDialog(getActivity(), getString(R.string.post_comment), getString(R.string.comment_posted));
                                        //Toast.makeText(getActivity(), getString(R.string.comment_posted), Toast.LENGTH_LONG).show();

                                    }
                                } catch (Throwable t) {
                                    t.printStackTrace();
                                    Log.e("<MAPP>", t.getMessage());
                                    Log.e("My App", "Could not parse malformed JSON: " + response + "");
                                }


                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                pDialog.hide();
                                Utils.showAlertDialog(getActivity(), "Response Error", error.getMessage());
                                //pDialog.setMessage(error.getMessage());
                               // pDialog.hide();
                                //mPostCommentResponse.requestEndedWithError(error);
                            }
                        }) {
                            @Override
                            protected Map<String, String> getParams() {
                                Map<String, String> params = new HashMap<String, String>();
                                params.put("author_name", name);
                                params.put("author_email", email);
                                params.put("content", comment);
                                return params;
                            }

                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                Map<String, String> params = new HashMap<String, String>();
                                params.put("Content-Type", "application/x-www-form-urlencoded");
//                                params.put("ApiKey", Const.AuthenticationKey);
                                return params;
                            }
                        };

                        // Adding request to volley request queue
                        AppController.getInstance().addToRequestQueue(sr);
                    }
                }else{
                    Toast.makeText(getActivity(), getString(R.string.comments_are_closed),Toast.LENGTH_LONG).show();
                }
            }
        });
        return v;
    }

    // validating email id
    private boolean isValidEmail(String email) {
        String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    // validating information
    private boolean isValidString(String pass, int length) {
        if (pass != null && pass.length() > length) {
            return true;
        }
        return false;
    }

}
