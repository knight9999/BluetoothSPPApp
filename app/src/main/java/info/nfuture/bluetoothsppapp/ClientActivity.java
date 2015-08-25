package info.nfuture.bluetoothsppapp;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

public class ClientActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
    }

    public static int MENU_ID_SERVER_MODE = 1;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem actionItem = menu.add(Menu.NONE,MENU_ID_SERVER_MODE,Menu.NONE,"Server Mode");
        actionItem.setIcon(android.R.drawable.ic_menu_manage);
        actionItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == MENU_ID_SERVER_MODE) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}
