package com.sunnykatiyar.appmanager;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FragmentHelp extends Fragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    TextView help_text ;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View v = inflater.inflate(R.layout.fragment_help,container,false);

        help_text = v.findViewById(R.id.help_textview);

        help_text.setText(" " +
                            "DOCUMENT_FLAGS : \n" +
                            "\n" +
                            "public static final int FLAG_SUPPORTS_WRITE = 1 << 1;\n" +
                            "public static final int FLAG_SUPPORTS_DELETE = 1 << 2;\n" +
                            "public static final int FLAG_DIR_SUPPORTS_CREATE = 1 << 3;\n" +
                            "public static final int FLAG_DIR_PREFERS_GRID = 1 << 4;\n" +
                            "public static final int FLAG_DIR_PREFERS_LAST_MODIFIED = 1 << 5;\n" +
                            "public static final int FLAG_SUPPORTS_RENAME = 1 << 6;\n" +
                            "public static final int FLAG_SUPPORTS_COPY = 1 << 7;\n" +
                            "public static final int FLAG_SUPPORTS_MOVE = 1 << 8;\n" +
                            "public static final int FLAG_VIRTUAL_DOCUMENT = 1 << 9;\n" +
                            "public static final int FLAG_SUPPORTS_REMOVE = 1 << 10;\n" +
                            "public static final int FLAG_SUPPORTS_SETTINGS = 1 << 11;\n" +
                            "public static final int FLAG_WEB_LINKABLE = 1 << 12;\n" +
                            "public static final int FLAG_PARTIAL = 1 << 16;\n" +
                            "public static final int FLAG_SUPPORTS_METADATA = 1 << 17;" +
                        "\n" +
                        "\n" +
                        "\n" +
                            "ROOT FLAGS: \n" +
                        "\n" +
                            "public static final int FLAG_SUPPORTS_CREATE = 1;\n" +
                            "public static final int FLAG_LOCAL_ONLY = 1 << 1;\n" +
                            "public static final int FLAG_SUPPORTS_RECENTS = 1 << 2;\n" +
                            "public static final int FLAG_SUPPORTS_SEARCH = 1 << 3;\n" +
                            "public static final int FLAG_SUPPORTS_IS_CHILD = 1 << 4;\n" +
                            "public static final int FLAG_SUPPORTS_EJECT = 1 << 5;\n" +
                            "public static final int FLAG_EMPTY = 1 << 16;\n" +
                            "public static final int FLAG_ADVANCED = 1 << 17;\n" +
                            "public static final int FLAG_HAS_SETTINGS = 1 << 18;\n" +
                            "public static final int FLAG_REMOVABLE_SD = 1 << 19;\n" +
                            "public static final int FLAG_REMOVABLE_USB = 1 << 20;" +
                        " \" ");

        return v;
    }
}
