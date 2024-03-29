package com.kmsoft.expensemanager.Fragment;

import static com.kmsoft.expensemanager.Constant.incomeAndExpenseArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.kmsoft.expensemanager.Activity.Trancation.DetailsTransactionActivity;
import com.kmsoft.expensemanager.Activity.Trancation.FinancialReportActivity;
import com.kmsoft.expensemanager.Adapter.DateAdapter;
import com.kmsoft.expensemanager.DBHelper;
import com.kmsoft.expensemanager.Model.IncomeAndExpense;
import com.kmsoft.expensemanager.Model.ListDateModel;
import com.kmsoft.expensemanager.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.stream.Collectors;

public class TransactionFragment extends Fragment {

    ImageView calender, filter, img;
    TextView see_financial_txt, emptyTransaction;
    IncomeAndExpense incomeAndExpense;
    RecyclerView dateRecyclerview;
    DateAdapter dateAdapter;
    ArrayList<IncomeAndExpense> incomeList;
    ArrayList<IncomeAndExpense> expenseList;
    DBHelper dbHelper;
    String filterBy;
    String sortBy;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String PREFS_NAME = "MyPrefsFile";
    String FILTER_KEY = "filter_by";
    String SORT_KEY = "sort_by";
    String date;
    int click = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction, container, false);

        init(view);

        dbHelper = new DBHelper(getContext());

        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        editor.putString(SORT_KEY, "");
        editor.putString(FILTER_KEY, "");
        editor.commit();

        filterBy = "All";
        sortBy = "Newest";

        calender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCalenderBottomDialog();
            }
        });

        see_financial_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FinancialReportActivity.class);
                startActivity(intent);
            }
        });

        filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterBottomDialog();
            }
        });

        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), DetailsTransactionActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Display();
    }

    private void showCalenderBottomDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.calender_bottomsheet_layout);
        dialog.setCancelable(false);
        dialog.show();

        TextView cancel = dialog.findViewById(R.id.cancel);
        TextView ok = dialog.findViewById(R.id.ok);
        CalendarView calendarView = dialog.findViewById(R.id.trans_calenderView);

        sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                Calendar selectedDateCalendar = Calendar.getInstance();
                selectedDateCalendar.set(year, month, dayOfMonth);
                SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                date = dateFormat1.format(selectedDateCalendar.getTime());
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
                if (TextUtils.isEmpty(date)) {
                    Calendar selectedDateCalendar = Calendar.getInstance();
                    SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    date = dateFormat1.format(selectedDateCalendar.getTime());
                }
                click = 1;
                Display();
                dialog.dismiss();
            }
        });
    }

    private void showFilterBottomDialog() {

        sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        filterBy = sharedPreferences.getString(FILTER_KEY, "");
        sortBy = sharedPreferences.getString(SORT_KEY, "");

        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.filter_bottomsheet_layout);
        dialog.show();

        TextView all = dialog.findViewById(R.id.trans_all);
        TextView income = dialog.findViewById(R.id.trans_income);
        TextView expense = dialog.findViewById(R.id.trans_expense);
        TextView highest = dialog.findViewById(R.id.trans_highest);
        TextView lowest = dialog.findViewById(R.id.trans_lowest);
        TextView newest = dialog.findViewById(R.id.trans_newest);
        TextView oldest = dialog.findViewById(R.id.trans_oldest);
        TextView reset = dialog.findViewById(R.id.trans_reset);
        Button apply = dialog.findViewById(R.id.trans_apply);

        switch (filterBy) {
            case "All":
                all.setBackgroundResource(R.drawable.selected_border_background);
                all.setTextColor(getResources().getColor(R.color.green));
                income.setBackgroundResource(R.drawable.unselected_border_background);
                income.setTextColor(Color.BLACK);
                expense.setBackgroundResource(R.drawable.unselected_border_background);
                expense.setTextColor(Color.BLACK);
                break;
            case "Income":
                income.setBackgroundResource(R.drawable.selected_border_background);
                income.setTextColor(getResources().getColor(R.color.green));
                all.setBackgroundResource(R.drawable.unselected_border_background);
                all.setTextColor(Color.BLACK);
                expense.setBackgroundResource(R.drawable.unselected_border_background);
                expense.setTextColor(Color.BLACK);
                break;
            case "Expense":
                expense.setBackgroundResource(R.drawable.selected_border_background);
                expense.setTextColor(getResources().getColor(R.color.green));
                all.setBackgroundResource(R.drawable.unselected_border_background);
                all.setTextColor(Color.BLACK);
                income.setBackgroundResource(R.drawable.unselected_border_background);
                income.setTextColor(Color.BLACK);
                break;
            default:
                all.setBackgroundResource(R.drawable.selected_border_background);
                all.setTextColor(getResources().getColor(R.color.green));
                income.setBackgroundResource(R.drawable.unselected_border_background);
                income.setTextColor(Color.BLACK);
                expense.setBackgroundResource(R.drawable.unselected_border_background);
                expense.setTextColor(Color.BLACK);
                break;
        }

        switch (sortBy) {
            case "Highest":
                highest.setBackgroundResource(R.drawable.selected_border_background);
                highest.setTextColor(getResources().getColor(R.color.green));

                lowest.setBackgroundResource(R.drawable.unselected_border_background);
                lowest.setTextColor(Color.BLACK);

                newest.setBackgroundResource(R.drawable.unselected_border_background);
                newest.setTextColor(Color.BLACK);

                oldest.setBackgroundResource(R.drawable.unselected_border_background);
                oldest.setTextColor(Color.BLACK);
                break;
            case "Lowest":
                lowest.setBackgroundResource(R.drawable.selected_border_background);
                lowest.setTextColor(getResources().getColor(R.color.green));

                highest.setBackgroundResource(R.drawable.unselected_border_background);
                highest.setTextColor(Color.BLACK);

                newest.setBackgroundResource(R.drawable.unselected_border_background);
                newest.setTextColor(Color.BLACK);

                oldest.setBackgroundResource(R.drawable.unselected_border_background);
                oldest.setTextColor(Color.BLACK);
                break;
            case "Newest":
                newest.setBackgroundResource(R.drawable.selected_border_background);
                newest.setTextColor(getResources().getColor(R.color.green));

                lowest.setBackgroundResource(R.drawable.unselected_border_background);
                lowest.setTextColor(Color.BLACK);

                highest.setBackgroundResource(R.drawable.unselected_border_background);
                highest.setTextColor(Color.BLACK);

                oldest.setBackgroundResource(R.drawable.unselected_border_background);
                oldest.setTextColor(Color.BLACK);
                break;
            case "Oldest":
                oldest.setBackgroundResource(R.drawable.selected_border_background);
                oldest.setTextColor(getResources().getColor(R.color.green));

                lowest.setBackgroundResource(R.drawable.unselected_border_background);
                lowest.setTextColor(Color.BLACK);

                newest.setBackgroundResource(R.drawable.unselected_border_background);
                newest.setTextColor(Color.BLACK);

                highest.setBackgroundResource(R.drawable.unselected_border_background);
                highest.setTextColor(Color.BLACK);
                break;
            default:
                newest.setBackgroundResource(R.drawable.selected_border_background);
                newest.setTextColor(getResources().getColor(R.color.green));

                lowest.setBackgroundResource(R.drawable.unselected_border_background);
                lowest.setTextColor(Color.BLACK);

                highest.setBackgroundResource(R.drawable.unselected_border_background);
                highest.setTextColor(Color.BLACK);

                oldest.setBackgroundResource(R.drawable.unselected_border_background);
                oldest.setTextColor(Color.BLACK);
                break;
        }

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterBy = "All";
                sortBy = "Newest";
                all.setBackgroundResource(R.drawable.selected_border_background);
                all.setTextColor(getResources().getColor(R.color.green));

                income.setBackgroundResource(R.drawable.unselected_border_background);
                income.setTextColor(Color.BLACK);

                expense.setBackgroundResource(R.drawable.unselected_border_background);
                expense.setTextColor(Color.BLACK);

                newest.setBackgroundResource(R.drawable.selected_border_background);
                newest.setTextColor(getResources().getColor(R.color.green));

                lowest.setBackgroundResource(R.drawable.unselected_border_background);
                lowest.setTextColor(Color.BLACK);

                highest.setBackgroundResource(R.drawable.unselected_border_background);
                highest.setTextColor(Color.BLACK);

                oldest.setBackgroundResource(R.drawable.unselected_border_background);
                oldest.setTextColor(Color.BLACK);
            }
        });

        all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterBy = "All";

                all.setBackgroundResource(R.drawable.selected_border_background);
                income.setBackgroundResource(R.drawable.unselected_border_background);
                expense.setBackgroundResource(R.drawable.unselected_border_background);

                all.setTextColor(getResources().getColor(R.color.green));
                income.setTextColor(Color.BLACK);
                expense.setTextColor(Color.BLACK);

            }
        });

        income.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterBy = "Income";

                income.setBackgroundResource(R.drawable.selected_border_background);
                all.setBackgroundResource(R.drawable.unselected_border_background);
                expense.setBackgroundResource(R.drawable.unselected_border_background);

                income.setTextColor(getResources().getColor(R.color.green));
                all.setTextColor(Color.BLACK);
                expense.setTextColor(Color.BLACK);
            }

        });

        expense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterBy = "Expense";

                expense.setBackgroundResource(R.drawable.selected_border_background);
                all.setBackgroundResource(R.drawable.unselected_border_background);
                income.setBackgroundResource(R.drawable.unselected_border_background);

                expense.setTextColor(getResources().getColor(R.color.green));
                all.setTextColor(Color.BLACK);
                income.setTextColor(Color.BLACK);
            }
        });

        newest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortBy = "Newest";

                newest.setBackgroundResource(R.drawable.selected_border_background);
                newest.setTextColor(getResources().getColor(R.color.green));

                lowest.setBackgroundResource(R.drawable.unselected_border_background);
                lowest.setTextColor(Color.BLACK);

                highest.setBackgroundResource(R.drawable.unselected_border_background);
                highest.setTextColor(Color.BLACK);

                oldest.setBackgroundResource(R.drawable.unselected_border_background);
                oldest.setTextColor(Color.BLACK);
            }
        });

        oldest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortBy = "Oldest";

                oldest.setBackgroundResource(R.drawable.selected_border_background);
                oldest.setTextColor(getResources().getColor(R.color.green));

                lowest.setBackgroundResource(R.drawable.unselected_border_background);
                lowest.setTextColor(Color.BLACK);

                newest.setBackgroundResource(R.drawable.unselected_border_background);
                newest.setTextColor(Color.BLACK);

                highest.setBackgroundResource(R.drawable.unselected_border_background);
                highest.setTextColor(Color.BLACK);
            }
        });

        highest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortBy = "Highest";

                highest.setBackgroundResource(R.drawable.selected_border_background);
                highest.setTextColor(getResources().getColor(R.color.green));

                lowest.setBackgroundResource(R.drawable.unselected_border_background);
                lowest.setTextColor(Color.BLACK);

                newest.setBackgroundResource(R.drawable.unselected_border_background);
                newest.setTextColor(Color.BLACK);

                oldest.setBackgroundResource(R.drawable.unselected_border_background);
                oldest.setTextColor(Color.BLACK);
            }
        });

        lowest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortBy = "Lowest";

                lowest.setBackgroundResource(R.drawable.selected_border_background);
                lowest.setTextColor(getResources().getColor(R.color.green));

                highest.setBackgroundResource(R.drawable.unselected_border_background);
                highest.setTextColor(Color.BLACK);

                newest.setBackgroundResource(R.drawable.unselected_border_background);
                newest.setTextColor(Color.BLACK);

                oldest.setBackgroundResource(R.drawable.unselected_border_background);
                oldest.setTextColor(Color.BLACK);
            }
        });

        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (filterBy.isEmpty()){
                    filterBy = "All";
                }
                if (sortBy.isEmpty()){
                    sortBy = "Newest";
                }
                editor.putString(FILTER_KEY, filterBy).apply();
                editor.putString(SORT_KEY, sortBy).apply();
                Display();
                dialog.dismiss();
            }
        });
    }

    private void Display() {
        Cursor cursor = dbHelper.getAllData();
        if (cursor != null && cursor.moveToFirst()) {
            incomeAndExpenseArrayList = new ArrayList<>();
            do {
                int id = cursor.getInt(0);
                String incomeAmount = cursor.getString(1);
                double currantDateTimeStamp = cursor.getDouble(2);
                double selectedDateTimeStamp = cursor.getDouble(3);
                String currentdate = cursor.getString(4);
                String incomeDate = cursor.getString(5);
                String incomeDay = cursor.getString(6);
                String incomeAddTime = cursor.getString(7);
                String categoryName = cursor.getString(8);
                int categoryImage = cursor.getInt(9);
                String incomeDescription = cursor.getString(10);
                String addAttachment = cursor.getString(11);
                String tag = cursor.getString(12);

                incomeAndExpense = new IncomeAndExpense(id, incomeAmount, currantDateTimeStamp, selectedDateTimeStamp, currentdate, incomeDate, incomeDay, incomeAddTime, categoryName, categoryImage, incomeDescription, addAttachment, tag);
                incomeAndExpenseArrayList.add(incomeAndExpense);

                incomeList = filterCategories(incomeAndExpenseArrayList, "Income");
                expenseList = filterCategories(incomeAndExpenseArrayList, "Expense");

                LinearLayoutManager manager = new LinearLayoutManager(getContext());

                if (click == 0) {
                    if (filterBy.equals("All")) {
                        if (sortBy.equals("Oldest")) {

                            Collections.sort(incomeAndExpenseArrayList, new Comparator<IncomeAndExpense>() {
                                @Override
                                public int compare(IncomeAndExpense d1, IncomeAndExpense d2) {
                                    return d1.getSelectedDateTimeStamp().compareTo(d2.getSelectedDateTimeStamp());
                                }
                            });

                        } else if (sortBy.equals("Newest")) {

                            Collections.sort(incomeAndExpenseArrayList, new Comparator<IncomeAndExpense>() {
                                @Override
                                public int compare(IncomeAndExpense d1, IncomeAndExpense d2) {
                                    return d1.getSelectedDateTimeStamp().compareTo(d2.getSelectedDateTimeStamp());
                                }
                            });
                            Collections.reverse(incomeAndExpenseArrayList);
                        } else {
                            Collections.sort(incomeAndExpenseArrayList, new Comparator<IncomeAndExpense>() {
                                @Override
                                public int compare(IncomeAndExpense d1, IncomeAndExpense d2) {
                                    return d1.getSelectedDateTimeStamp().compareTo(d2.getSelectedDateTimeStamp());
                                }
                            });
                            Collections.reverse(incomeAndExpenseArrayList);
                        }

                        ArrayList<ListDateModel> listDateModels = new ArrayList<>();
                        String date = "";
                        for (int i = 0; i < incomeAndExpenseArrayList.size(); i++) {
                            String convertDate = convertTimestampToDateString(incomeAndExpenseArrayList.get(i).getSelectedDateTimeStamp(), "dd/MM/yyyy");
                            if (!convertDate.equalsIgnoreCase(date)) {
                                date = convertDate;
                                ArrayList<IncomeAndExpense> newList = (ArrayList<IncomeAndExpense>) incomeAndExpenseArrayList.stream().filter(model -> model.getDate().equalsIgnoreCase(convertDate)).collect(Collectors.toList());

                                ListDateModel listDateModel = new ListDateModel();
                                listDateModel.incomeAndExpenseArrayList = newList;

                                listDateModels.add(listDateModel);
                            }
                        }
                        if (incomeAndExpenseArrayList.isEmpty()) {
                            emptyTransaction.setVisibility(View.VISIBLE);
                            dateRecyclerview.setVisibility(View.GONE);
                        } else {
                            dateRecyclerview.setVisibility(View.VISIBLE);
                            emptyTransaction.setVisibility(View.GONE);
                            dateAdapter = new DateAdapter(TransactionFragment.this.getContext(), listDateModels, sortBy);
                            dateRecyclerview.setLayoutManager(manager);
                            dateRecyclerview.setAdapter(dateAdapter);
                        }
                    } else if (filterBy.equals("Income")) {
                        if (sortBy.equals("Oldest")) {
                            Collections.sort(incomeList, new Comparator<IncomeAndExpense>() {
                                @Override
                                public int compare(IncomeAndExpense d1, IncomeAndExpense d2) {
                                    return d1.getSelectedDateTimeStamp().compareTo(d2.getSelectedDateTimeStamp());
                                }
                            });
                        } else if (sortBy.equals("Newest")) {
                            Collections.sort(incomeList, new Comparator<IncomeAndExpense>() {
                                @Override
                                public int compare(IncomeAndExpense d1, IncomeAndExpense d2) {
                                    return d1.getSelectedDateTimeStamp().compareTo(d2.getSelectedDateTimeStamp());
                                }
                            });

                            Collections.reverse(incomeList);
                        } else {
                            Collections.sort(incomeList, new Comparator<IncomeAndExpense>() {
                                @Override
                                public int compare(IncomeAndExpense d1, IncomeAndExpense d2) {
                                    return d1.getSelectedDateTimeStamp().compareTo(d2.getSelectedDateTimeStamp());
                                }
                            });

                            Collections.reverse(incomeList);
                        }

                        ArrayList<ListDateModel> listDateModels = new ArrayList<>();
                        String date = "";
                        for (int i = 0; i < incomeList.size(); i++) {
                            String convertDate = convertTimestampToDateString(incomeList.get(i).getSelectedDateTimeStamp(), "dd/MM/yyyy");
                            if (!convertDate.equalsIgnoreCase(date)) {
                                date = convertDate;
                                ArrayList<IncomeAndExpense> newList = (ArrayList<IncomeAndExpense>) incomeList.stream().filter(model -> model.getDate().equalsIgnoreCase(convertDate)).collect(Collectors.toList());

                                ListDateModel listDateModel = new ListDateModel();
                                listDateModel.incomeAndExpenseArrayList = newList;

                                listDateModels.add(listDateModel);
                            }
                        }
                        if (incomeList.isEmpty()) {
                            emptyTransaction.setVisibility(View.VISIBLE);
                            dateRecyclerview.setVisibility(View.GONE);
                        } else {
                            dateRecyclerview.setVisibility(View.VISIBLE);
                            emptyTransaction.setVisibility(View.GONE);
                            dateAdapter = new DateAdapter(TransactionFragment.this.getContext(), listDateModels, sortBy);
                            dateRecyclerview.setLayoutManager(manager);
                            dateRecyclerview.setAdapter(dateAdapter);
                        }
                    } else if (filterBy.equals("Expense")) {
                        if (sortBy.equals("Oldest")) {
                            Collections.sort(expenseList, new Comparator<IncomeAndExpense>() {
                                @Override
                                public int compare(IncomeAndExpense d1, IncomeAndExpense d2) {
                                    return d1.getSelectedDateTimeStamp().compareTo(d2.getSelectedDateTimeStamp());
                                }
                            });
                        } else if (sortBy.equals("Newest")) {
                            Collections.sort(expenseList, new Comparator<IncomeAndExpense>() {
                                @Override
                                public int compare(IncomeAndExpense d1, IncomeAndExpense d2) {
                                    return d1.getSelectedDateTimeStamp().compareTo(d2.getSelectedDateTimeStamp());
                                }
                            });

                            Collections.reverse(expenseList);
                        } else {
                            Collections.sort(expenseList, new Comparator<IncomeAndExpense>() {
                                @Override
                                public int compare(IncomeAndExpense d1, IncomeAndExpense d2) {
                                    return d1.getSelectedDateTimeStamp().compareTo(d2.getSelectedDateTimeStamp());
                                }
                            });

                            Collections.reverse(expenseList);
                        }

                        ArrayList<ListDateModel> listDateModels = new ArrayList<>();
                        String date = "";
                        for (int i = 0; i < expenseList.size(); i++) {
                            String convertDate = convertTimestampToDateString(expenseList.get(i).getSelectedDateTimeStamp(), "dd/MM/yyyy");
                            if (!convertDate.equalsIgnoreCase(date)) {
                                date = convertDate;
                                ArrayList<IncomeAndExpense> newList = (ArrayList<IncomeAndExpense>) expenseList.stream().filter(model -> model.getDate().equalsIgnoreCase(convertDate)).collect(Collectors.toList());

                                ListDateModel listDateModel = new ListDateModel();
                                listDateModel.incomeAndExpenseArrayList = newList;

                                listDateModels.add(listDateModel);
                            }
                        }
                        if (expenseList.isEmpty()) {
                            emptyTransaction.setVisibility(View.VISIBLE);
                            dateRecyclerview.setVisibility(View.GONE);
                        } else {
                            dateRecyclerview.setVisibility(View.VISIBLE);
                            emptyTransaction.setVisibility(View.GONE);
                            dateAdapter = new DateAdapter(TransactionFragment.this.getContext(), listDateModels, sortBy);
                            dateRecyclerview.setLayoutManager(manager);
                            dateRecyclerview.setAdapter(dateAdapter);
                        }
                    }
                } else if (click == 1) {
                    ArrayList<IncomeAndExpense> incomeAndExpenseArrayList1 = filterDataByDate(incomeAndExpenseArrayList,date);

                    Collections.sort(incomeAndExpenseArrayList1, new Comparator<IncomeAndExpense>() {
                        @Override
                        public int compare(IncomeAndExpense d1, IncomeAndExpense d2) {
                            return d1.getSelectedDateTimeStamp().compareTo(d2.getSelectedDateTimeStamp());
                        }
                    });

                    Collections.reverse(incomeAndExpenseArrayList1);

                    ArrayList<ListDateModel> listDateModels = new ArrayList<>();
                    String date = "";
                    for (int i = 0; i < incomeAndExpenseArrayList1.size(); i++) {
                        String convertDate = convertTimestampToDateString(incomeAndExpenseArrayList1.get(i).getSelectedDateTimeStamp(), "dd/MM/yyyy");
                        if (!convertDate.equalsIgnoreCase(date)) {
                            date = convertDate;
                            ArrayList<IncomeAndExpense> newList = (ArrayList<IncomeAndExpense>) incomeAndExpenseArrayList1.stream().filter(model -> model.getDate().equalsIgnoreCase(convertDate)).collect(Collectors.toList());

                            ListDateModel listDateModel = new ListDateModel();
                            listDateModel.incomeAndExpenseArrayList = newList;

                            listDateModels.add(listDateModel);
                        }
                    }
                    if (incomeAndExpenseArrayList.isEmpty()) {
                        emptyTransaction.setVisibility(View.VISIBLE);
                        dateRecyclerview.setVisibility(View.GONE);
                    } else {
                        dateRecyclerview.setVisibility(View.VISIBLE);
                        emptyTransaction.setVisibility(View.GONE);
                        dateAdapter = new DateAdapter(TransactionFragment.this.getContext(), listDateModels, sortBy);
                        dateRecyclerview.setLayoutManager(manager);
                        dateRecyclerview.setAdapter(dateAdapter);
                    }
                }
            }
            while (cursor.moveToNext());
        } else {
            incomeAndExpenseArrayList = new ArrayList<>();
            dateRecyclerview.setVisibility(View.GONE);
            emptyTransaction.setVisibility(View.VISIBLE);
        }
        click = 0;
    }

    private ArrayList<IncomeAndExpense> filterCategories(ArrayList<IncomeAndExpense> incomeAndExpenses, String type) {
        ArrayList<IncomeAndExpense> filteredList = new ArrayList<>();
        for (IncomeAndExpense incomeAndExpense : incomeAndExpenses) {
            if (incomeAndExpense.getTag().equals(type)) {
                filteredList.add(incomeAndExpense);
            }
        }
        return filteredList;
    }

    private ArrayList<IncomeAndExpense> filterDataByDate(ArrayList<IncomeAndExpense> incomeAndExpenses, String targetDate) {
        ArrayList<IncomeAndExpense> filteredList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        try {
            if (TextUtils.isEmpty(targetDate)){
                Date currentDate = new Date();
                targetDate = sdf.format(currentDate);
            }
            Date targetDateObj = sdf.parse(targetDate);
            for (IncomeAndExpense data : incomeAndExpenses) {
                Date currentDateObj = sdf.parse(data.getDate());
                if (currentDateObj.equals(targetDateObj)) {
                    filteredList.add(data);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return filteredList;
    }


    public String convertTimestampToDateString(double timestamp, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            long time = (long) (timestamp * 1000);
            Date date = new Date(time);

            return sdf.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private void init(View view) {
        calender = view.findViewById(R.id.trans_calender);
        filter = view.findViewById(R.id.trans_filter);
        img = view.findViewById(R.id.img);
        see_financial_txt = view.findViewById(R.id.see_financial_txt);
        dateRecyclerview = view.findViewById(R.id.date_recyclerview);
        emptyTransaction = view.findViewById(R.id.empty_transaction);
    }
}