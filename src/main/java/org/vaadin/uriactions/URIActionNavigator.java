package org.vaadin.uriactions;

import org.roklib.util.helper.CheckForNull;
import org.roklib.webapps.uridispatching.AbstractURIActionCommand;
import org.roklib.webapps.uridispatching.AbstractURIActionHandler;
import org.roklib.webapps.uridispatching.URIActionDispatcher;

import com.vaadin.navigator.NavigationStateManager;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.Navigator.ComponentContainerViewDisplay;
import com.vaadin.navigator.Navigator.SingleComponentContainerViewDisplay;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.navigator.ViewDisplay;
import com.vaadin.navigator.ViewProvider;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.SingleComponentContainer;
import com.vaadin.ui.UI;

public class URIActionNavigator
{
  private Navigator                mNavigator;
  private URIActionViewDisplay     mViewDisplay;
  private URIActionViewProvider    mViewProvider;
  private URIActionDispatcher      mURIActionDispatcher;
  private AbstractURIActionCommand mCurrentAction;
  private String                   mCurrentViewAndParameters;

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
    this (ui, navigationStateManager, useCaseSensitiveURIs, (ViewDisplay) null);
  }

  public URIActionNavigator (UI ui, NavigationStateManager navigationStateManager, boolean useCaseSensitiveURIs,
      ComponentContainer container)
  {
    this (ui, navigationStateManager, useCaseSensitiveURIs, new ComponentContainerViewDisplay (container));
  }

  public URIActionNavigator (UI ui, NavigationStateManager navigationStateManager, boolean useCaseSensitiveURIs,
      SingleComponentContainer container)
  {
    this (ui, navigationStateManager, useCaseSensitiveURIs, new SingleComponentContainerViewDisplay (container));
  }

  public URIActionNavigator (UI ui, NavigationStateManager navigationStateManager, boolean useCaseSensitiveURIs,
      ViewDisplay viewDisplay)
  {
    mViewDisplay = new URIActionViewDisplay (viewDisplay);
    mViewProvider = new URIActionViewProvider ();

    if (navigationStateManager != null)
    {
      mNavigator = new Navigator (ui, navigationStateManager, mViewDisplay);
    } else
    {
      mNavigator = new Navigator (ui, mViewDisplay);
    }
    mNavigator.addProvider (mViewProvider);
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

  void resetCurrentAction ()
  {
    mCurrentAction = null;
  }

  public void replay ()
  {
    mNavigator.navigateTo (mCurrentViewAndParameters);
  }

  private class URIActionViewDisplay implements ViewDisplay
  {
    private ViewDisplay mUserProvidedDisplay;

    public URIActionViewDisplay (ViewDisplay userProvidedDisplay)
    {
      mUserProvidedDisplay = userProvidedDisplay;
    }

    @Override
    public void showView (View view)
    {
      if (view instanceof ActionExecutionView)
      {
        // Nothing to do in this case. Action command is executed in ActionExecutionView.enter().
        return;
      } else if (mUserProvidedDisplay != null)
      {
        mUserProvidedDisplay.showView (view);
      }
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
      if (action != null)
      {
        mCurrentViewAndParameters = viewAndParameters;
        return viewAndParameters;
      } else
      {
        mCurrentViewAndParameters = null;
        return null;
      }
    }

    @Override
    public View getView (String viewName)
    {
      return new ActionExecutionView (mCurrentAction);
    }
  }

  public class ActionExecutionView implements View
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
      resetCurrentAction ();
    }

    public AbstractURIActionCommand getURIActionCommand ()
    {
      return mCommand;
    }
  }

  public String getCurrentlyHandledURI ()
  {
    return mCurrentViewAndParameters;
  }

  public URIActionDispatcher getURIActionDispatcher ()
  {
    return mURIActionDispatcher;
  }
}
