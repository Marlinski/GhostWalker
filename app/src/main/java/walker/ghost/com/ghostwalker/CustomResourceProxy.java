package walker.ghost.com.ghostwalker;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import org.osmdroid.DefaultResourceProxyImpl;

public class CustomResourceProxy extends DefaultResourceProxyImpl {

    private final Context mContext;
    public CustomResourceProxy(Context pContext) {
        super(pContext);
        mContext = pContext;
    }

    @Override
    public Bitmap getBitmap(final bitmap pResId) {
        return BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.pokeball8bits);
    }

    @Override
    public Drawable getDrawable(final bitmap pResId) {
        return mContext.getResources().getDrawable(R.mipmap.pokeball8bits);
    }
}