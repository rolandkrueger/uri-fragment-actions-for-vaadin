package org.vaadin.uriactions;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.roklib.webapps.uridispatching.AbstractURIActionCommand;
import org.roklib.webapps.uridispatching.SimpleURIActionHandler;
import org.vaadin.uriactions.testhelpers.TestNavigationStateHandler;
import org.vaadin.uriactions.testhelpers.TestUI;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.navigator.ViewDisplay;

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

  @Test
  public void testProvideOwnViewDisplay ()
  {
    TestViewDisplay viewDisplay = new TestViewDisplay ();
    mTestObj = new URIActionNavigator (new TestUI (), mNavigationStateHandler, true, viewDisplay);
    TestActionCommand cmd = new TestActionCommand ();
    mTestObj.addHandler (new SimpleURIActionHandler ("test", cmd));
    mTestObj.getNavigator ().addView ("separate_view", new View ()
    {
      @Override
      public void enter (ViewChangeEvent event)
      {
      }
    });

    mTestObj.getNavigator ().navigateTo ("separate_view");
    assertTrue ("Separately provided view display was not activated.", viewDisplay.viewShown);
    assertFalse ("Action command was unexpectedly executed.", cmd.isExecuted ());
  }

  private static class TestViewDisplay implements ViewDisplay
  {
    public boolean viewShown = false;

    @Override
    public void showView (View view)
    {
      viewShown = true;
    }
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
