package com.mariniu.core.events.base;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created on 01/07/16.
 *
 * @author Umberto Marini
 */
public class EventDelegate implements Parcelable {

    private String mOwner;

    public EventDelegate() {
        // empty constructor
    }

    public EventDelegate(String owner) {
        mOwner = owner;
    }

    public EventDelegate(Class owner) {
        mOwner = owner != null ? owner.getSimpleName() : null;
    }

    /**
     * The owner of this {@code BaseRequestEvent}.
     */
    public String getOwner() {
        return mOwner;
    }

    /**
     * Sets the owner of this {@code OwnerEvent}.
     */
    public void setOwner(String owner) {
        mOwner = owner;
    }

    /**
     * Sets the owner of this {@code OwnerEvent}.
     */
    public void setOwner(Class owner) {
        mOwner = owner != null ? owner.getSimpleName() : null;
    }

    public boolean matchOwnership(String otherOwner) {
        if (mOwner == null && otherOwner == null) {
            return true;
        }

        if (mOwner == null) {
            return false;
        }

        if (otherOwner == null) {
            return false;
        }

        return mOwner.equals(otherOwner);
    }

    public boolean matchOwnership(Class otherOwner) {
        return matchOwnership(otherOwner != null ? otherOwner.getSimpleName() : null);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mOwner);
    }

    protected EventDelegate(Parcel in) {
        this.mOwner = in.readString();
    }

    public static final Creator<EventDelegate> CREATOR = new Creator<EventDelegate>() {
        @Override
        public EventDelegate createFromParcel(Parcel source) {
            return new EventDelegate(source);
        }

        @Override
        public EventDelegate[] newArray(int size) {
            return new EventDelegate[size];
        }
    };
}
