package lerignoux.droid_control;

import android.support.v4.view.PagerAdapter;
import android.view.View;

/**
 * Created by laurent on 6/9/17.
 */

public class TabPageAdapter extends PagerAdapter {

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return false;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 1) {
            return "Audio";
        }
        if (position == 2) {
            return "Video";
        }
        return "Main";
    }
}
