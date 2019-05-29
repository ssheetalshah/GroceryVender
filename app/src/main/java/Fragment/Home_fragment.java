package Fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Adapter.DealsAdapter;
import Adapter.Home_adapter;
import Adapter.HorizontalAdapter;
import Adapter.OfferAdapter;
import Adapter.RecyclerViewHorizontalListAdapter;
import Config.BaseURL;
import Model.Category_model;
import Model.Data;
import Model.DealsModel;
import Model.Grocery;
import Model.OfferModel;

import com.aryanonline.AppController;
import com.aryanonline.MainActivity;
import com.aryanonline.R;

import util.ConnectivityReceiver;
import util.CustomVolleyJsonRequest;
import util.HttpHandler;
import util.RecyclerTouchListener;


public class Home_fragment extends Fragment {

    private static String TAG = Home_fragment.class.getSimpleName();
    private SliderLayout imgSlider;
    private RecyclerView rv_items;
    //private RelativeLayout rl_view_all;
    private List<Category_model> category_modelList = new ArrayList<>();
    private Home_adapter adapter;
    private ImageView iv_location;
    private boolean isSubcat = false;
    EditText searchview;
    RecyclerView offerlist;
    HorizontalAdapter horizontalAdapter;
    private List<Data> data = new ArrayList<>();
    RecyclerView dealsview;
    private List<Grocery> groceryList = new ArrayList<>();
    private RecyclerView groceryRecyclerView;
    private RecyclerViewHorizontalListAdapter groceryAdapter;
    private String server_url;
    ArrayList<OfferModel> offer_list;
    private OfferAdapter offerAdapter;
    String product_image;
    ArrayList<DealsModel> deals_list;
    private DealsAdapter dealsAdapter;
    // ImageView imageNew;

    public Home_fragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        setHasOptionsMenu(true);

        ((MainActivity) getActivity()).setTitle(getResources().getString(R.string.app_name));
        ((MainActivity) getActivity()).updateHeader();

        // handle the touch event if true
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // check user can press back button or not
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {

                    ((MainActivity) getActivity()).finish();

                    return true;
                }
                return false;
            }
        });

        imgSlider = (SliderLayout) view.findViewById(R.id.home_img_slider);
        rv_items = (RecyclerView) view.findViewById(R.id.rv_home);
        offerlist = (RecyclerView) view.findViewById(R.id.offerlist);
        searchview = (EditText) view.findViewById(R.id.searchview);
        dealsview = (RecyclerView) view.findViewById(R.id.dealsview);
        //   imageNew =  (ImageView) view.findViewById(R.id.imgNew);

        offer_list = new ArrayList<>();
        deals_list = new ArrayList<>();
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);
        rv_items.setLayoutManager(gridLayoutManager);

        //rv_items.setLayoutManager(new LinearLayoutManager(getActivity()));

        // initialize a SliderLayout
        imgSlider.setPresetTransformer(SliderLayout.Transformer.Accordion);
        imgSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        imgSlider.setCustomAnimation(new DescriptionAnimation());
        imgSlider.setDuration(4000);

        // check internet connection
        if (ConnectivityReceiver.isConnected()) {
            makeGetSliderRequest();
            makeGetCategoryRequest("");
        }

        if (ConnectivityReceiver.isConnected()) {
            new GetOfferlist().execute();
        } else {
            Toast.makeText(getActivity(), "No Internet", Toast.LENGTH_SHORT).show();
        }

        if (ConnectivityReceiver.isConnected()) {
            new GetTodaysDealslist().execute();
        } else {
            Toast.makeText(getActivity(), "No Internet", Toast.LENGTH_SHORT).show();
        }

        rv_items.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), rv_items, new RecyclerTouchListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                String getid = category_modelList.get(position).getId();
                String getcat_title = category_modelList.get(position).getTitle();

                Bundle args = new Bundle();
                Fragment fm = new Product_fragment();
                args.putString("cat_id", getid);
                args.putString("cat_title", getcat_title);
                fm.setArguments(args);
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.contentPanel, fm)
                        .addToBackStack(null).commit();

            }

            @Override
            public void onLongItemClick(View view, int position) {

            }
        }));

        searchview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fm = new Search_fragment();
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.contentPanel, fm)
                        .addToBackStack(null).commit();
            }
        });

        return view;
    }

    private void populategroceryList() {
        Grocery potato = new Grocery("Laptop 20% Off", R.drawable.lap);
        Grocery onion = new Grocery("Television 15% Off", R.drawable.tv);
        Grocery cabbage = new Grocery("Mobile 30% Off", R.drawable.mobile);
        Grocery cauliflower = new Grocery("Speaker 20% Off", R.drawable.speak);
        groceryList.add(potato);
        groceryList.add(onion);
        groceryList.add(cabbage);
        groceryList.add(cauliflower);
        groceryAdapter.notifyDataSetChanged();
    }

    /**
     * Method to make json array request where json response starts wtih {
     */
    private void makeGetSliderRequest() {

        JsonArrayRequest req = new JsonArrayRequest(BaseURL.GET_SLIDER_URL,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(TAG, response.toString());

                        try {

                            ArrayList<HashMap<String, String>> listarray = new ArrayList<>();

                            for (int i = 0; i < response.length(); i++) {

                                JSONObject jsonObject = (JSONObject) response
                                        .get(i);

                                HashMap<String, String> url_maps = new HashMap<String, String>();
                                url_maps.put("slider_title", jsonObject.getString("slider_title"));
                                url_maps.put("slider_image", BaseURL.IMG_SLIDER_URL + jsonObject.getString("slider_image"));

                                listarray.add(url_maps);
                            }

                            for (HashMap<String, String> name : listarray) {
                                TextSliderView textSliderView = new TextSliderView(getActivity());
                                // initialize a SliderLayout
                                textSliderView
                                        //   .description(name.get("slider_title"))
                                        .image(name.get("slider_image"))
                                        .setScaleType(BaseSliderView.ScaleType.Fit);

                                //add your extra information
                                textSliderView.bundle(new Bundle());
                                textSliderView.getBundle()
                                        .putString("extra", name.get("slider_title"));

                                imgSlider.addSlider(textSliderView);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getActivity(),
                                    "Error: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.connection_time_out), Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(req);

    }

    /**
     * Method to make json object request where json response starts wtih {
     */
    private void makeGetCategoryRequest(String parent_id) {

        // Tag used to cancel the request
        String tag_json_obj = "json_category_req";

        isSubcat = false;

        Map<String, String> params = new HashMap<String, String>();
        if (parent_id != null && parent_id != "") {
            params.put("parent", parent_id);
            isSubcat = true;
        }

        CustomVolleyJsonRequest jsonObjReq = new CustomVolleyJsonRequest(Request.Method.POST,
                BaseURL.GET_CATEGORY_URL, params, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());

                try {
                    Boolean status = response.getBoolean("responce");
                    if (status) {

                        Gson gson = new Gson();
                        Type listType = new TypeToken<List<Category_model>>() {
                        }.getType();

                        category_modelList = gson.fromJson(response.getString("data"), listType);

                        adapter = new Home_adapter(category_modelList);
                        rv_items.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.connection_time_out), Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Add your menu entries here
        super.onCreateOptionsMenu(menu, inflater);

       /* MenuItem search = menu.findItem(R.id.action_search);
        search.setVisible(true);*/
        MenuItem check = menu.findItem(R.id.action_change_password);
        check.setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_search:
              /*  Fragment fm = new Search_fragment();
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.contentPanel, fm)
                        .addToBackStack(null).commit();
                return false;*/
        }
        return false;
    }

    //----------------------------------------------------------------------

    class GetOfferlist extends AsyncTask<String, String, String> {
        String output = "";
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(getActivity());
            dialog.setMessage("Processing");
            dialog.setCancelable(true);
            dialog.show();
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... params) {

            try {
                server_url = "http://axomiyagohona.com/grocery-store/index.php/Api/get_pro_offer";
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.e("sever_url>>>>>>>>>", server_url);
            output = HttpHandler.makeServiceCall(server_url);
            //   Log.e("getcomment_url", output);
            System.out.println("getcomment_url" + output);
            return output;
        }

        @Override
        protected void onPostExecute(String output) {
            if (output == null) {
                dialog.dismiss();
            } else {
                try {
                    dialog.dismiss();

                    JSONArray Data_array = new JSONArray(output);
                    for (int i = 0; i < Data_array.length(); i++) {
                        JSONObject c = Data_array.getJSONObject(i);
                        String product_id = c.getString("product_id");
                        String product_name = c.getString("product_name");
                        String product_description = c.getString("product_description");
                        String product_image = c.getString("product_image");
                        String category_id = c.getString("category_id");
                        String in_stock = c.getString("in_stock");
                        String price = c.getString("price");
                        String unit_value = c.getString("unit_value");
                        String unit = c.getString("unit");
                        String increament = c.getString("increament");
                        String Mrp = c.getString("Mrp");
                        String today_deals = c.getString("today_deals");
                        String offers_cat = c.getString("offers_cat");
                        String deals_description = c.getString("deals_description");
                        String offers_cat_desc = c.getString("offers_cat_desc");
                        String emi = c.getString("emi");
                        String warranty = c.getString("warranty");
                        offer_list.add(new OfferModel(product_id, product_name, product_description, product_image, category_id, in_stock, price
                                , unit_value, unit, increament, Mrp, today_deals, offers_cat, deals_description, offers_cat_desc, emi, warranty));
                    }

                    offerAdapter = new OfferAdapter(getActivity(), offer_list);
                    LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
                    offerlist.setLayoutManager(mLayoutManager);
                    offerlist.setItemAnimator(new DefaultItemAnimator());
                    offerlist.setAdapter(offerAdapter);

                } catch (JSONException e) {
                    e.printStackTrace();
                    //  dialog.dismiss();
                }
                super.onPostExecute(output);
            }
        }
    }

    //----------------------------------------------

    class GetTodaysDealslist extends AsyncTask<String, String, String> {
        String output = "";
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(getActivity());
            dialog.setMessage("Processing");
            dialog.setCancelable(true);
            dialog.show();
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... params) {

            try {
                server_url = "http://axomiyagohona.com/grocery-store/index.php/Api/get_today_deals";
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.e("sever_url>>>>>>>>>", server_url);
            output = HttpHandler.makeServiceCall(server_url);
            //   Log.e("getcomment_url", output);
            System.out.println("getcomment_url" + output);
            return output;
        }

        @Override
        protected void onPostExecute(String output) {
            if (output == null) {
                dialog.dismiss();
            } else {
                try {
                    dialog.dismiss();

                    JSONArray Data_array = new JSONArray(output);
                    for (int i = 0; i < Data_array.length(); i++) {
                        JSONObject c = Data_array.getJSONObject(i);
                        String product_id = c.getString("product_id");
                        String product_name = c.getString("product_name");
                        String product_description = c.getString("product_description");
                        product_image = c.getString("product_image");
                        String category_id = c.getString("category_id");
                        String in_stock = c.getString("in_stock");
                        String price = c.getString("price");
                        String unit_value = c.getString("unit_value");
                        String unit = c.getString("unit");
                        String increament = c.getString("increament");
                        String Mrp = c.getString("Mrp");
                        String today_deals = c.getString("today_deals");
                        String offers_cat = c.getString("offers_cat");
                        String deals_description = c.getString("deals_description");
                        String offers_cat_desc = c.getString("offers_cat_desc");
                        String emi = c.getString("emi");
                        String warranty = c.getString("warranty");
                        deals_list.add(new DealsModel(product_id, product_name, product_description, product_image, category_id, in_stock, price
                                , unit_value, unit, increament, Mrp, today_deals, offers_cat, deals_description, offers_cat_desc, emi, warranty));
                    }
                    /*    AnimationDrawable animation = new AnimationDrawable();
                        try {
                            animation.addFrame((Drawable) Glide.with(getActivity())
                                    .load(BaseURL.IMG_PRODUCT_URL+product_image)
                                    .placeholder(R.drawable.shop)
                                    .crossFade()
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .dontAnimate()
                                    .into(imageNew), 1000);

                        }catch (Exception e)
                        {
                            Log.e("product_image>>>>",""+product_image);
                        }
                        animation.setOneShot(false);
                        imageNew.setBackgroundDrawable(animation);

                        // start the animation!
                        animation.start();
                    }*/

                    dealsAdapter = new DealsAdapter(getActivity(), deals_list);
                    LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
                    dealsview.setLayoutManager(mLayoutManager);
                    dealsview.setItemAnimator(new DefaultItemAnimator());
                    dealsview.setAdapter(dealsAdapter);

                } catch (JSONException e) {
                    e.printStackTrace();
                    //  dialog.dismiss();
                }
                super.onPostExecute(output);
            }
        }
    }

}
