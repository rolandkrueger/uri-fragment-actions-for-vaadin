package org.vaadin.uriactions;

import org.roklib.util.helper.CheckForNull;
import org.roklib.webapps.uridispatching.AbstractURIActionCommand;
import org.roklib.webapps.uridispatching.IURIActionHandler.ParameterMode;
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
  private ParameterMode            mParameterMode = ParameterMode.DIRECTORY_WITH_NAMES;

  public URIActionNavigator (UI ui)
  {
    this (ui, null);
  }

  public URIActionNavigator (UI ui, NavigationStateManager navigationStateManager)
  {
    this (ui, navigationStateManager, (ViewDisplay) null);
  }

  public URIActionNavigator (UI ui, NavigationStateManager navigationStateManager, ComponentContainer container)
  {
    this (ui, navigationStateManager, new ComponentContainerViewDisplay (container));
  }

  public URIActionNavigator (UI ui, NavigationStateManager navigationStateManager, SingleComponentContainer container)
  {
    this (ui, navigationStateManager, new SingleComponentContainerViewDisplay (container));
  }

  public URIActionNavigator (UI ui, NavigationStateManager navigationStateManager, ViewDisplay viewDisplay)
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
  }

  public void setParameterMode (ParameterMode parameterMode)
  {
    CheckForNull.check (parameterMode);
    mParameterMode = parameterMode;
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
      checkURIActionDispatcher ();
      AbstractURIActionCommand action = mURIActionDispatcher.getActionForURI (viewAndParameters, mParameterMode);
      if (mCurrentAction != null)
      {
        throw new IllegalStateException (
            "Thread synchronization problem: this action navigator is currently handling another request. Current action is: "
                + mCurrentAction);
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
      resetCurrentAction ();
      mCommand.execute ();
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

  public boolean hasURIActionDispatcher ()
  {
    return mURIActionDispatcher != null;
  }

  public URIActionDispatcher getURIActionDispatcher ()
  {
    return mURIActionDispatcher;
  }

  public void setURIActionDispatcher (URIActionDispatcher dispatcher)
  {
    mURIActionDispatcher = dispatcher;
  }

  private void checkURIActionDispatcher ()
  {
    if (!hasURIActionDispatcher ())
    {
      throw new IllegalStateException ("No URI action dispatcher has been set for this object yet.");
    }
  }
}
