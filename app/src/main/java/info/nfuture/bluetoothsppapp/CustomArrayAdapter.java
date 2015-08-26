package info.nfuture.bluetoothsppapp;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.List;

/**
 * Created by knaito on 2015/08/26.
 */
public class CustomArrayAdapter<T> extends ArrayAdapter<T> {

    List<T> mObjects;

    public CustomArrayAdapter(Context context, int resource, List<T> objects) {
        super(context, resource, objects);
        mObjects = objects;
    }

    public List<T> getList() {
        return mObjects;
    }

    public boolean contains(T target) {
        return mObjects.contains(target);
    }
}
