package org.vaadin.uriactions;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.roklib.webapps.uridispatching.AbstractURIActionCommand;
import org.roklib.webapps.uridispatching.SimpleURIActionHandler;
import org.vaadin.uriactions.testhelpers.TestNavigationStateHandler;
import org.vaadin.uriactions.testhelpers.TestUI;

public class URIActionNavigatorTest
{
  private URIActionNavigator         mTestObj;
  private TestNavigationStateHandler mNavigationStateHandler;

  @Before
  public void setUp ()
  {
    mNavigationStateHandler = new TestNavigationStateHandler ();
    mTestObj = new URIActionNavigator (new TestUI (), mNavigationStateHandler, true);
  }

  @Test
  public void testAddHandler ()
  {
    TestActionCommand cmd = new TestActionCommand ();
    mTestObj.addHandler (new SimpleURIActionHandler ("test", cmd));
    mNavigationStateHandler.setState ("/test");
    mTestObj.getNavigator ().navigateTo ("/test");
    assertTrue ("Action command was not executed.", cmd.isExecuted ());
  }

  private static class TestActionCommand extends AbstractURIActionCommand
  {
    private boolean executed = false;

    @Override
    public void execute ()
    {
      executed = true;
    }

    public boolean isExecuted ()
    {
      return executed;
    }
  }
}
