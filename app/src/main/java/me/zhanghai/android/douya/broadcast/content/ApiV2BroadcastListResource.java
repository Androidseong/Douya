/*
 * Copyright (c) 2017 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.douya.broadcast.content;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import java.util.Collections;
import java.util.List;

import me.zhanghai.android.douya.content.MoreRawListResourceFragment;
import me.zhanghai.android.douya.network.api.ApiError;
import me.zhanghai.android.douya.network.api.ApiRequest;
import me.zhanghai.android.douya.network.api.ApiService;
import me.zhanghai.android.douya.network.api.info.apiv2.Broadcast;
import me.zhanghai.android.douya.util.FragmentUtils;

public class ApiV2BroadcastListResource
        extends MoreRawListResourceFragment<List<Broadcast>, Broadcast> {

    private static final String KEY_PREFIX = ApiV2BroadcastListResource.class.getName() + '.';

    private static final String EXTRA_USER_ID_OR_UID = KEY_PREFIX + "user_id_or_uid";
    private static final String EXTRA_TOPIC = KEY_PREFIX + "topic";

    private String mUserIdOrUid;
    private String mTopic;

    private static final String FRAGMENT_TAG_DEFAULT = ApiV2BroadcastListResource.class.getName();

    private static ApiV2BroadcastListResource newInstance(String userIdOrUid, String topic) {
        //noinspection deprecation
        return new ApiV2BroadcastListResource().setArguments(userIdOrUid, topic);
    }

    public static ApiV2BroadcastListResource attachTo(String userIdOrUid, String topic,
                                                      Fragment fragment, String tag,
                                                      int requestCode) {
        FragmentActivity activity = fragment.getActivity();
        ApiV2BroadcastListResource instance = FragmentUtils.findByTag(activity, tag);
        if (instance == null) {
            instance = newInstance(userIdOrUid, topic);
            instance.targetAt(fragment, requestCode);
            FragmentUtils.add(instance, activity, tag);
        }
        return instance;
    }

    public static ApiV2BroadcastListResource attachTo(String userIdOrUid, String topic,
                                                      Fragment fragment) {
        return attachTo(userIdOrUid, topic, fragment, FRAGMENT_TAG_DEFAULT, REQUEST_CODE_INVALID);
    }

    /**
     * @deprecated Use {@code attachTo()} instead.
     */
    public ApiV2BroadcastListResource() {}

    protected ApiV2BroadcastListResource setArguments(String userIdOrUid, String topic) {
        Bundle arguments = FragmentUtils.ensureArguments(this);
        arguments.putString(EXTRA_USER_ID_OR_UID, userIdOrUid);
        arguments.putString(EXTRA_TOPIC, topic);
        return this;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        mUserIdOrUid = arguments.getString(EXTRA_USER_ID_OR_UID);
        mTopic = arguments.getString(EXTRA_TOPIC);
    }

    @Override
    protected ApiRequest<List<Broadcast>> onCreateRequest(boolean more, int count) {
        Long untilId = null;
        if (more && has()) {
            List<Broadcast> broadcastList = get();
            int size = broadcastList.size();
            if (size > 0) {
                untilId = broadcastList.get(size - 1).id;
            }
        }
        return ApiService.getInstance().getApiV2BroadcastList(mUserIdOrUid, mTopic, untilId, count);
    }

    @Override
    protected ApiRequest<List<Broadcast>> onCreateRequest(Integer start, Integer count) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void onLoadStarted() {
        getListener().onLoadApiV2BroadcastListStarted(getRequestCode());
    }

    @Override
    protected void onLoadFinished(boolean more, int count, boolean successful,
                                  List<Broadcast> response, ApiError error) {
        if (successful) {
            if (more) {
                append(response);
                getListener().onLoadApiV2BroadcastListFinished(getRequestCode());
                getListener().onApiV2BroadcastListAppended(getRequestCode(),
                        Collections.unmodifiableList(response));
            } else {
                set(response);
                getListener().onLoadApiV2BroadcastListFinished(getRequestCode());
                getListener().onApiV2BroadcastListChanged(getRequestCode(),
                        Collections.unmodifiableList(response));
            }
        } else {
            getListener().onLoadApiV2BroadcastListFinished(getRequestCode());
            getListener().onLoadApiV2BroadcastListError(getRequestCode(), error);
        }
    }

    private Listener getListener() {
        return (Listener) getTarget();
    }

    public interface Listener {
        void onLoadApiV2BroadcastListStarted(int requestCode);
        void onLoadApiV2BroadcastListFinished(int requestCode);
        void onLoadApiV2BroadcastListError(int requestCode, ApiError error);
        /**
         * @param newBroadcastList Unmodifiable.
         */
        void onApiV2BroadcastListChanged(int requestCode, List<Broadcast> newBroadcastList);
        /**
         * @param appendedBroadcastList Unmodifiable.
         */
        void onApiV2BroadcastListAppended(int requestCode, List<Broadcast> appendedBroadcastList);
    }
}