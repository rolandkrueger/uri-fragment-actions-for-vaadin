package org.vaadin.uriactions;

import org.roklib.webapps.uridispatching.URIActionDispatcher;

import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
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

  public URIActionNavigator (UI ui)
  {
    this (ui, true);
  }

  public URIActionNavigator (UI ui, boolean useCaseSensitiveURIs)
  {
    mViewDisplay = new URIActionViewDisplay ();
    mViewProvider = new URIActionViewProvider ();
    mErrorViewProvider = new URIActionErrorViewProvider ();

    mNavigator = new Navigator (ui, mViewDisplay);
    mNavigator.addProvider (mViewProvider);
    mNavigator.setErrorProvider (mErrorViewProvider);

    mURIActionDispatcher = new URIActionDispatcher (useCaseSensitiveURIs);
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
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public View getView (String viewName)
    {
      // TODO Auto-generated method stub
      return null;
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
      // TODO Auto-generated method stub
      return null;
    }

  }

}
