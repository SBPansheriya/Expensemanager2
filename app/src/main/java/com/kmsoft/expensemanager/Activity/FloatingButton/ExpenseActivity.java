package com.kmsoft.expensemanager.Activity.FloatingButton;

import static android.Manifest.permission_group.CAMERA;
import static com.kmsoft.expensemanager.Activity.SplashActivity.CLICK_KEY;
import static com.kmsoft.expensemanager.Activity.SplashActivity.PREFS_NAME;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.kmsoft.expensemanager.DBHelper;
import com.kmsoft.expensemanager.Model.IncomeAndExpense;
import com.kmsoft.expensemanager.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ExpenseActivity extends AppCompatActivity {

    DBHelper dbHelper;
    ArrayList<IncomeAndExpense> incomeAndExpenseArrayList;
    IncomeAndExpense incomeAndExpense;
    ImageView back, calender, setExpenseImage, removeExpenseImage;
    TextView showExpenseDate, expenseCategoryName;
    EditText expenseAddAmount, expenseDescription;
    Button expenseAdded;
    RelativeLayout expenseSetImage;
    LinearLayout expenseCategory, expenseAddAttachment;
    String currantDate;
    String selectedDate;
    String dayName;
    Bitmap bitmap;
    ActivityResultLauncher<Intent> launchSomeActivity;
    ActivityResultLauncher<CropImageContractOptions> cropImage;
    ActivityResultLauncher<Intent> launchSomeActivityResult;
    private static final int CAMERA_REQUEST = 101;
    int click;
    int imageResId;
    double selectedDateTimeStamp;
    String categoryName;
    String expenseAddTime;
    String addAttachmentImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);
        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getSupportActionBar().hide();

        init();

        dbHelper = new DBHelper(this);
        incomeAndExpenseArrayList = new ArrayList<>();

        launchSomeActivityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                if (data != null) {
                    imageResId = data.getIntExtra("categoryImage", 0);
                    categoryName = data.getStringExtra("categoryName");
                    if (!TextUtils.isEmpty(categoryName)) {
                        expenseCategoryName.setText("" + categoryName);
                    }
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        calender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCalenderBottomDialog();
            }
        });

        expenseCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ExpenseActivity.this, AddCategoryActivity.class);
                intent.putExtra("clicked","Expense");
                launchSomeActivityResult.launch(intent);
            }
        });

        expenseAddAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionsForCamera();
            }
        });

        expenseAddAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString();
                if (!input.startsWith("₹")) {
                    expenseAddAmount.setText("₹" + input);
                    expenseAddAmount.setSelection(expenseAddAmount.getText().length());
                }
            }
        });

        expenseAdded.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String editTextValue = expenseAddAmount.getText().toString();
                if (editTextValue.equals("₹0") || editTextValue.equals("₹")) {
                    Toast.makeText(ExpenseActivity.this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(editTextValue)){
                    Toast.makeText(ExpenseActivity.this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(showExpenseDate.getText().toString())) {
                    Toast.makeText(ExpenseActivity.this, "Please enter a date", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(expenseCategoryName.getText().toString())) {
                    Toast.makeText(ExpenseActivity.this, "Please enter a valid category", Toast.LENGTH_SHORT).show();
                } else {
                    String amount = expenseAddAmount.getText().toString();
                    String description = expenseDescription.getText().toString();
                    double currantDateTimeStamp = Calendar.getInstance().getTimeInMillis()/1000;
                    incomeAndExpense = new IncomeAndExpense(0, amount, currantDateTimeStamp,selectedDateTimeStamp,currantDate, selectedDate, dayName,expenseAddTime, categoryName, imageResId, description, addAttachmentImage, "Expense");
                    incomeAndExpenseArrayList.add(incomeAndExpense);
                    dbHelper.insertData(incomeAndExpense);
                    Dialog dialog = new Dialog(ExpenseActivity.this);
                    if (dialog.getWindow() != null) {
                        dialog.getWindow().setGravity(Gravity.CENTER);
                        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                        dialog.setCancelable(false);
                    }
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    dialog.setContentView(R.layout.dailog_removed_layout);
                    dialog.setCancelable(true);
                    dialog.show();

                    TextView txt = dialog.findViewById(R.id.txt);
                    txt.setText("Expense has been successfully added");

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (dialog.isShowing()) {
                                onBackPressed();
                                dialog.dismiss();
                            }
                        }
                    }, 1000);
                }
            }
        });

        launchSomeActivity = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Uri selectedImageUri = result.getData().getData();
                startCrop(selectedImageUri);
            }
        });

        removeExpenseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expenseAddAttachment.setVisibility(View.VISIBLE);
                expenseSetImage.setVisibility(View.GONE);
            }
        });

        cropImage = registerForActivityResult(new CropImageContract(), result -> {
            if (result.isSuccessful()) {
                Uri uriContent = result.getUriContent();
                bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uriContent);
                    addAttachmentImage = (MediaStore.Images.Media.insertImage(this.getContentResolver(), bitmap, "Title", null));

                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (bitmap != null) {
                    setExpenseImage.setImageBitmap(bitmap);
                }
                expenseAddAttachment.setVisibility(View.GONE);
                expenseSetImage.setVisibility(View.VISIBLE);
            } else {
                expenseAddAttachment.setVisibility(View.VISIBLE);
                expenseSetImage.setVisibility(View.GONE);
            }
        });
    }

    private void startCrop(Uri selectedImageUri) {
        CropImageOptions options = new CropImageOptions();
        options.guidelines = CropImageView.Guidelines.ON;
        CropImageContractOptions cropImageContractOptions = new CropImageContractOptions(selectedImageUri, options);
        cropImage.launch(cropImageContractOptions);
    }

    private void openImagePicker() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        launchSomeActivity.launch(i);
    }

    private void cameraPermission() {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePicture, CAMERA_REQUEST);
    }

    private void showAttachmentBottomDialog(){
        BottomSheetDialog dialog = new BottomSheetDialog(ExpenseActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.add_attachment_bottomsheet_layout);
        dialog.setCancelable(true);
        dialog.show();

        ImageView camera = dialog.findViewById(R.id.camera);
        ImageView gallery = dialog.findViewById(R.id.gallery);

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraPermission();
                dialog.dismiss();
            }
        });

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
                dialog.dismiss();
            }
        });
    }

    private void checkPermissionsForCamera() {
        String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(ExpenseActivity.this, permissions, 100);
        } else {
            showAttachmentBottomDialog();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            showAttachmentBottomDialog();
        } else {
            showPermissionDenyDialog(ExpenseActivity.this,100);
        }
    }

    private void showPermissionDenyDialog(Activity activity, int requestCode) {

        SharedPreferences settings = activity.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        click = settings.getInt(CLICK_KEY, 0);

        if (click == 0) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, CAMERA)) {

                Dialog dialog = new Dialog(activity);
                if (dialog.getWindow() != null) {
                    dialog.getWindow().setGravity(Gravity.CENTER);
                    dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    dialog.setCancelable(false);
                }
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                dialog.setContentView(R.layout.permission_denied_first_dialog);
                dialog.setCancelable(false);
                dialog.show();

                Button cancel = dialog.findViewById(R.id.canceldialog);
                Button ok = dialog.findViewById(R.id.okdialog);

                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        checkPermissionsForCamera();
                        dialog.dismiss();
                    }
                });
            }
            click = 1;
        }
        else if (click == 1) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, CAMERA)) {

                Dialog dialog = new Dialog(activity);
                if (dialog.getWindow() != null) {
                    dialog.getWindow().setGravity(Gravity.CENTER);
                    dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    dialog.setCancelable(false);
                }
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                dialog.setContentView(R.layout.permission_denied_first_dialog);
                dialog.setCancelable(false);
                dialog.show();

                Button cancel = dialog.findViewById(R.id.canceldialog);
                Button ok = dialog.findViewById(R.id.okdialog);
                TextView textView = dialog.findViewById(R.id.filename);

                textView.setText(R.string.camera_permission);
                ok.setText("Enable from settings");

                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                        intent.setData(uri);
                        ActivityCompat.startActivityForResult(ExpenseActivity.this,intent, requestCode,null);
                        dialog.dismiss();
                    }
                });
            }
        }
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(CLICK_KEY, click);
        editor.apply();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            checkPermissionsForCamera();
        } else if (requestCode == CAMERA_REQUEST) {
            if (data != null && data.getExtras() != null && data.getExtras().containsKey("data")) {
                bitmap = (Bitmap) data.getExtras().get("data");
                if (bitmap != null) {
                    String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "title", null);
                    startCrop(Uri.parse(path));
                }
            } else {
                Toast.makeText(this, "No image data found in the intent.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showCalenderBottomDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(ExpenseActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.calender_bottomsheet_layout);
        dialog.setCancelable(false);
        dialog.show();

        TextView cancel = dialog.findViewById(R.id.cancel);
        TextView ok = dialog.findViewById(R.id.ok);
        CalendarView calendarView = dialog.findViewById(R.id.trans_calenderView);

        long currentDateMillis = System.currentTimeMillis();
        calendarView.setMaxDate(currentDateMillis);

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                Calendar currentTime = Calendar.getInstance();
                currentTime.set(year, month, dayOfMonth);
                Date currentDate = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                currantDate = sdf.format(currentDate);

                int dayOfWeek = currentTime.get(Calendar.DAY_OF_WEEK);
                String[] daysOfWeek = new DateFormatSymbols().getWeekdays();
                dayName = daysOfWeek[dayOfWeek];
                SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                expenseAddTime = dateFormat.format(currentTime.getTime());

                Calendar selectedDateCalendar = Calendar.getInstance();
                selectedDateCalendar.set(year, month, dayOfMonth);
                SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                selectedDate = dateFormat1.format(selectedDateCalendar.getTime());
                selectedDateTimeStamp = selectedDateCalendar.getTimeInMillis()/1000;
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(selectedDate)){
                    Calendar currentTime = Calendar.getInstance();
                    Date currentDate = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    currantDate = sdf.format(currentDate);

                    int dayOfWeek = currentTime.get(Calendar.DAY_OF_WEEK);
                    String[] daysOfWeek = new DateFormatSymbols().getWeekdays();
                    dayName = daysOfWeek[dayOfWeek];
                    SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                    expenseAddTime = dateFormat.format(currentTime.getTime());

                    Calendar selectedDateCalendar = Calendar.getInstance();
                    SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    selectedDate = dateFormat1.format(selectedDateCalendar.getTime());
                    selectedDateTimeStamp = selectedDateCalendar.getTimeInMillis()/1000;
                    showExpenseDate.setText(selectedDate);
                } else {
                    showExpenseDate.setText(""+selectedDate);
                }
                dialog.dismiss();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent();
        intent.putExtra("incomeAndExpense",incomeAndExpense);
        setResult(RESULT_OK, intent);
    }

    private void init() {
        back = findViewById(R.id.back);
        calender = findViewById(R.id.expense_calender);
        expenseCategory = findViewById(R.id.expense_category);
        expenseAddAttachment = findViewById(R.id.expense_add_attachment);
        showExpenseDate = findViewById(R.id.show_expense_date);
        setExpenseImage = findViewById(R.id.set_expense_image);
        expenseSetImage = findViewById(R.id.expense_set_image);
        removeExpenseImage = findViewById(R.id.remove_expense_image);
        expenseAdded = findViewById(R.id.expense_added);
        expenseAddAmount = findViewById(R.id.expense_add_amount);
        expenseCategoryName = findViewById(R.id.expense_category_name);
        expenseDescription = findViewById(R.id.expense_description);
    }
}