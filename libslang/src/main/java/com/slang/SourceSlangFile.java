package com.slang;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by syanochara on 27/07/2017.
 */

public class SourceSlangFile extends SlangFile {
    public SourceSlangFile(String filename, String raw) {
        super(filename, raw);
    }

    public ArrayList<ArrayList<String>> doSearch(String query) throws Exception {
        return (ArrayList<ArrayList<String>>)super.interpretMethod("search",query);
    }
}
