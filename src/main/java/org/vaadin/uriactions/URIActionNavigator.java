package org.vaadin.uriactions;

import org.roklib.util.helper.CheckForNull;
import org.roklib.webapps.uridispatching.AbstractURIActionCommand;
import org.roklib.webapps.uridispatching.AbstractURIActionHandler;
import org.roklib.webapps.uridispatching.URIActionDispatcher;

import com.vaadin.navigator.NavigationStateManager;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.navigator.ViewDisplay;
import com.vaadin.navigator.ViewProvider;
import com.vaadin.ui.UI;

public class URIActionNavigator
{
  private Navigator                  mNavigator;
  private URIActionViewDisplay       mViewDisplay;
  private URIActionViewProvider      mViewProvider;
  private URIActionErrorViewProvider mErrorViewProvider;
  private URIActionDispatcher        mURIActionDispatcher;
  private AbstractURIActionCommand   mCurrentAction;

  public URIActionNavigator (UI ui)
  {
    this (ui, true);
  }

  public URIActionNavigator (UI ui, boolean useCaseSensitiveURIs)
  {
    this (ui, null, useCaseSensitiveURIs);
  }

  public URIActionNavigator (UI ui, NavigationStateManager navigationStateManager, boolean useCaseSensitiveURIs)
  {
    mViewDisplay = new URIActionViewDisplay ();
    mViewProvider = new URIActionViewProvider ();
    mErrorViewProvider = new URIActionErrorViewProvider ();

    if (navigationStateManager != null)
    {
      mNavigator = new Navigator (ui, navigationStateManager, mViewDisplay);
    } else
    {
      mNavigator = new Navigator (ui, mViewDisplay);
    }
    mNavigator.addProvider (mViewProvider);
    mNavigator.setErrorProvider (mErrorViewProvider);

    mURIActionDispatcher = new URIActionDispatcher (useCaseSensitiveURIs);
  }

  public final void addHandler (AbstractURIActionHandler subHandler)
  {
    mURIActionDispatcher.addHandler (subHandler);
  }

  public Navigator getNavigator ()
  {
    return mNavigator;
  }

  private class URIActionViewDisplay implements ViewDisplay
  {
    @Override
    public void showView (View view)
    {
      // TODO Auto-generated method stub
    }
  }

  private class URIActionViewProvider implements ViewProvider
  {
    @Override
    public String getViewName (String viewAndParameters)
    {
      AbstractURIActionCommand action = mURIActionDispatcher.getActionForURI (viewAndParameters);
      if (mCurrentAction != null)
      {
        throw new IllegalStateException (
            "Thread synchronization problem: this action navigator is currently handling another request.");
      }
      mCurrentAction = action;
      return action == null ? null : viewAndParameters;
    }

    @Override
    public View getView (String viewName)
    {
      return new ActionExecutionView (mCurrentAction);
    }
  }

  private class URIActionErrorViewProvider implements ViewProvider
  {
    @Override
    public String getViewName (String viewAndParameters)
    {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public View getView (String viewName)
    {
      return null;
    }
  }

  private class ActionExecutionView implements View
  {
    private AbstractURIActionCommand mCommand;

    public ActionExecutionView (AbstractURIActionCommand command)
    {
      CheckForNull.check (command);
      mCommand = command;
    }

    @Override
    public void enter (ViewChangeEvent event)
    {
      mCommand.execute ();
      mCurrentAction = null;
    }
  }
}
