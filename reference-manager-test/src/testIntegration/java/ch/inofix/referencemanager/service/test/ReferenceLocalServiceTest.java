package ch.inofix.referencemanager.service.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.rule.Sync;
import com.liferay.portal.kernel.test.rule.SynchronousDestinationTestRule;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.service.test.ServiceTestUtil;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import ch.inofix.referencemanager.model.Reference;
import ch.inofix.referencemanager.service.ReferenceLocalServiceUtil;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Christian Berndt
 */
@RunWith(Arquillian.class)
@Sync
public class ReferenceLocalServiceTest {

    @ClassRule
    @Rule
    public static final AggregateTestRule aggregateTestRule = new AggregateTestRule(new LiferayIntegrationTestRule(),
            SynchronousDestinationTestRule.INSTANCE);

    @Before
    public void setUp() throws Exception {
        ServiceTestUtil.setUser(TestPropsValues.getUser());

        _group = GroupTestUtil.addGroup();
        _user = TestPropsValues.getUser();

    }

    @Test
    public void testCreateBibliography() throws Exception {

        Reference reference = ReferenceLocalServiceUtil.createReference(0);

        Assert.assertNotNull(reference);
    }

    private static final Log _log = LogFactoryUtil.getLog(ReferenceLocalServiceTest.class.getName());

    @DeleteAfterTestRun
    private Group _group;

    private User _user;

}