package com.golive.cinema.kdm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.golive.cinema.player.kdm.KDM;
import com.golive.player.kdm.KDMDeviceID;
import com.golive.player.kdm.KDMResCode;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import rx.observers.TestSubscriber;

/**
 * Integration test for the {@link KDM}
 * Created by Wangzj on 2016/9/22.
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class KDMTest {

    private KDM mKDM;

    @Before
    public void setup() {
        mKDM = new KDM(InstrumentationRegistry.getTargetContext(), KDMDeviceID.CompanyType.TCL);
    }

    @After
    public void cleanup() {
        mKDM = null;
    }

    @Test
    public void testGetKdmVersion() {
        TestSubscriber<KDMResCode> testSubscriber = new TestSubscriber<>();
        mKDM.getKdmVersion().toBlocking().subscribe(testSubscriber);
        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();
        final KDMResCode resCode = testSubscriber.getOnNextEvents().get(0);
//        final KDMResCode resCode = mKDM.getKdmVersion().toBlocking().first();
        assertNotNull(resCode);
        assertEquals(KDMResCode.RESCODE_OK, resCode.getResult());
        assertNotNull(resCode.version);
        assertNotNull(resCode.version.getVersion());
        assertNotNull(resCode.version.getPlatform());
    }

    @Test
    public void testInitKdm() {
        String regUrl = "http://www.cloudmovie.net.cn:9090";
        TestSubscriber<KDMResCode> testSubscriber = new TestSubscriber<>();
        mKDM.initKdm(regUrl, false).toBlocking().subscribe(testSubscriber);
        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();
        final KDMResCode resCode = testSubscriber.getOnNextEvents().get(0);
//        final KDMResCode resCode = mKDM.initKdm(regUrl, false).toBlocking().first();
        assertNotNull(resCode);
        assertEquals(KDMResCode.RESCODE_OK, resCode.getResult());
        assertNotNull(resCode.init);
    }
}
