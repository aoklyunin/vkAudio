package com.vk.vktestapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/*
    Этот фрагмент создан для того, чтобы в функции newInstance передавать
    номер страницы и тип, а на входе получать фрагмент для соответствующей страницы.
 */
public class PageFragment extends Fragment {
    static final String ARGUMENT_PAGE_NUMBER = "arg_page_number";
    static final String ARGUMENT_AUDIO_TYPE = "arg_audio_type";
    int pageNumber;
    String type;
    static PageFragment newInstance(int page,String type) {
        // создаём новый фрагмент
        PageFragment pageFragment = new PageFragment();
        // задаём новые параметры
        Bundle arguments = new Bundle();
        arguments.putInt(ARGUMENT_PAGE_NUMBER, page);
        arguments.putString(ARGUMENT_AUDIO_TYPE, type);
        pageFragment.setArguments(arguments);
        // возвращаем созданный фрагмент
        return pageFragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageNumber = getArguments().getInt(ARGUMENT_PAGE_NUMBER);
        type = getArguments().getString(ARGUMENT_AUDIO_TYPE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment, null);
        LinearLayout tvPage = (LinearLayout) view.findViewById(R.id.tvPage);
        DBHelper db = new DBHelper(getActivity(),type);
        tvPage.addView(db.getPageByPos(pageNumber));
        return view;
    }
}