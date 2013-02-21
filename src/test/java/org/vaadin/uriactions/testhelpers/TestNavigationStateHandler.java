package org.vaadin.uriactions.testhelpers;

import com.vaadin.navigator.NavigationStateManager;
import com.vaadin.navigator.Navigator;

public class TestNavigationStateHandler implements NavigationStateManager
{
  private String mState;

  @Override
  public String getState ()
  {
    return mState;
  }

  @Override
  public void setState (String state)
  {
    mState = state;
  }

  @Override
  public void setNavigator (Navigator navigator)
  {
  }
}
