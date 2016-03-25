/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.virial.cluster2.mvc;

public interface WizardView extends View {

  public void attachPageView(WizardPageView page);

  public void detachPageView(WizardPageView page);

  public void close();
}