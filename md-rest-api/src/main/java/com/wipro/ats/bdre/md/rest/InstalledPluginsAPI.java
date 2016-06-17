/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wipro.ats.bdre.md.rest;

import com.wipro.ats.bdre.md.api.base.MetadataAPIBase;
import com.wipro.ats.bdre.md.dao.InstalledPluginsDAO;
import com.wipro.ats.bdre.md.dao.jpa.InstalledPlugins;
import com.wipro.ats.bdre.md.rest.util.BindingResultError;
import com.wipro.ats.bdre.md.rest.util.DateConverter;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by PR324290 on 3/8/2016.
 */
@Controller
@RequestMapping("/installedplugins")
public class InstalledPluginsAPI extends MetadataAPIBase {

    private static final Logger LOGGER = Logger.getLogger(InstalledPluginsAPI.class);
    private static final String RECORDWITHID = "Record with ID:";
    @Autowired
    InstalledPluginsDAO installedPluginsDAO;

    /**
     * This method calls proc GetDeployStatus and fetches a record from DeployStatus table corresponding
     * to deployStatusId passed.
     *
     * @param
     * @return restWrapper It contains an instance of pluginDependency corresponding to deployStatusId passed.
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody public
    RestWrapper get(
            @PathVariable("id") String pluginUniqueId, Principal principal
    ) {

        RestWrapper restWrapper = null;
        try {
            com.wipro.ats.bdre.md.dao.jpa.InstalledPlugins jpaInstalledPlugins = installedPluginsDAO.get(pluginUniqueId);
            InstalledPlugins installedPlugins = new InstalledPlugins();
            if (jpaInstalledPlugins != null) {
                installedPlugins.setPluginUniqueId(jpaInstalledPlugins.getPluginUniqueId());
                installedPlugins.setPlugin(jpaInstalledPlugins.getPlugin());
                installedPlugins.setAddTs(jpaInstalledPlugins.getAddTs());
                installedPlugins.setAuthor(jpaInstalledPlugins.getAuthor());
                installedPlugins.setDescription(jpaInstalledPlugins.getDescription());
                installedPlugins.setPluginId(jpaInstalledPlugins.getPluginId());
                installedPlugins.setName(jpaInstalledPlugins.getName());
            }
            restWrapper = new RestWrapper(installedPlugins, RestWrapper.OK);
            LOGGER.info(RECORDWITHID + pluginUniqueId + " selected from AppDeploymentQueueStatus by User:" + principal.getName());
        }catch (Exception e) {
            LOGGER.error( e);
            return new RestWrapper(e.getMessage(), RestWrapper.ERROR);
        }
        return restWrapper;

    }

    /**
     * This method calls DeleteDeployStatus and fetches a record corresponding to the deployStatusId passed.
     *
     * @param pluginUniqueId
     * @return nothing.
     */
    @RequestMapping(value = "/{id}/", method = RequestMethod.DELETE)
    @ResponseBody public
    RestWrapper delete(
            @PathVariable("id") String pluginUniqueId, Principal principal) {
        RestWrapper restWrapper = null;
        try {
            installedPluginsDAO.delete(pluginUniqueId);
            restWrapper = new RestWrapper(null, RestWrapper.OK);
            LOGGER.info(RECORDWITHID + pluginUniqueId + " deleted from AppDeploymentQueueStatus by User:" + principal.getName());
        } catch (Exception e) {
            LOGGER.error( e);
            return new RestWrapper(e.getMessage(), RestWrapper.ERROR);
        }
        return restWrapper;
    }

    /**
     * This method calls proc ListStatusDeploy and fetches a list of DeployStatus records.
     *
     * @param
     * @return restWrapper It contains list of instances of DeployStatus.
     */
    @RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
    @ResponseBody public
    RestWrapper list(@RequestParam(value = "page", defaultValue = "0") int startPage,
                     @RequestParam(value = "size", defaultValue = "10") int pageSize, Principal principal) {
        RestWrapper restWrapper = null;
        try {
            Integer counter=installedPluginsDAO.totalRecordCount().intValue();
            List<com.wipro.ats.bdre.md.dao.jpa.InstalledPlugins> jpaInstalledPlugins = installedPluginsDAO.list(startPage, pageSize);
            List<com.wipro.ats.bdre.md.beans.table.InstalledPlugins> installedPluginsList = new ArrayList<com.wipro.ats.bdre.md.beans.table.InstalledPlugins>();

            for (com.wipro.ats.bdre.md.dao.jpa.InstalledPlugins installedPlugins1 : jpaInstalledPlugins) {
                com.wipro.ats.bdre.md.beans.table.InstalledPlugins installedPlugins2 = new com.wipro.ats.bdre.md.beans.table.InstalledPlugins();
               installedPlugins2.setAuthor(installedPlugins1.getAuthor());
                installedPlugins2.setPlugin(installedPlugins1.getPlugin());
                installedPlugins2.setName(installedPlugins1.getName());
                installedPlugins2.setDescription(installedPlugins1.getDescription());
                installedPlugins2.setTableAddTs(DateConverter.dateToString(installedPlugins1.getAddTs()));
                installedPlugins2.setPluginUniqueId(installedPlugins1.getPluginUniqueId());
                installedPlugins2.setPluginId(installedPlugins1.getPluginId());
                installedPlugins2.setVersion(installedPlugins1.getPluginVersion());
                installedPlugins2.setCounter(counter);
                installedPluginsList.add(installedPlugins2);
            }
            restWrapper = new RestWrapper(installedPluginsList, RestWrapper.OK);
            LOGGER.info("All records listed from DeployStatus by User:" + principal.getName());
        } catch (Exception e) {
            LOGGER.error( e);
            return new RestWrapper(e.getMessage(), RestWrapper.ERROR);
        }
        return restWrapper;
    }

    /**
     * This method calls UpdateDeployStatus and updates the record passed. It also validates
     * the values of the record passed.
     *
     * @param installedPlugins  Instance of DeployStatus.
     * @param bindingResult
     * @return restWrapper The updated instance of DeployStatus.
     */
    @RequestMapping(value = {"/", ""}, method = RequestMethod.POST)
    @ResponseBody public
    RestWrapper update(@ModelAttribute("installedplugins")
                       @Valid com.wipro.ats.bdre.md.beans.table.InstalledPlugins installedPlugins, BindingResult bindingResult, Principal principal) {
        LOGGER.debug("Entering into update for adq_status table");
        RestWrapper restWrapper = null;
        if (bindingResult.hasErrors()) {
            BindingResultError bindingResultError = new BindingResultError();
            return bindingResultError.errorMessage(bindingResult);
        }
        try {
            LOGGER.info("installed plugin is "+installedPlugins);
            InstalledPlugins jpaInstalledPlugins=installedPluginsDAO.get(installedPlugins.getPluginUniqueId());
            LOGGER.info("installed plugin is "+installedPlugins);
              jpaInstalledPlugins.setAuthor(installedPlugins.getAuthor());
              jpaInstalledPlugins.setName(installedPlugins.getName());
              jpaInstalledPlugins.setDescription(installedPlugins.getDescription());
              jpaInstalledPlugins.setPluginVersion(installedPlugins.getVersion());
              jpaInstalledPlugins.setPlugin(installedPlugins.getPlugin());
            installedPluginsDAO.update(jpaInstalledPlugins);
            restWrapper = new RestWrapper(installedPlugins, RestWrapper.OK);
            LOGGER.info(RECORDWITHID + installedPlugins.getPluginUniqueId() + " updated in AppDeploymentQueueStatus by User:" + principal.getName() );
        } catch (Exception e) {
            LOGGER.error( e);
            return new RestWrapper(e.getMessage(), RestWrapper.ERROR);
        }
        return restWrapper;
    }

    /**
     * This method calls proc InsertDeployStatus and adds a new record in the database. It also validates the
     * values passed.
     *
     * @param installedPlugins  Instance of DeployStatus.
     * @param bindingResult
     * @return restWrapper Instance of DeployStatus passed.
     */
    @RequestMapping(value = {"/", ""}, method = RequestMethod.PUT)
    @ResponseBody public
    RestWrapper insert(@ModelAttribute("installedplugins")
                       @Valid com.wipro.ats.bdre.md.beans.table.InstalledPlugins installedPlugins, BindingResult bindingResult, Principal principal) {
        LOGGER.debug("Entering into insert for adq_status table");
        RestWrapper restWrapper = null;
        if (bindingResult.hasErrors()) {
            BindingResultError bindingResultError = new BindingResultError();
            return bindingResultError.errorMessage(bindingResult);
        }

        try {
            com.wipro.ats.bdre.md.dao.jpa.InstalledPlugins jpaInstalledPlugins = new com.wipro.ats.bdre.md.dao.jpa.InstalledPlugins();
            jpaInstalledPlugins.setAuthor(installedPlugins.getAuthor());
            jpaInstalledPlugins.setPlugin(installedPlugins.getPlugin());
            jpaInstalledPlugins.setName(installedPlugins.getName());
            jpaInstalledPlugins.setDescription(installedPlugins.getDescription());
            jpaInstalledPlugins.setAddTs(new Date());
            jpaInstalledPlugins.setPluginId(installedPlugins.getPluginId());
            jpaInstalledPlugins.setPluginUniqueId(installedPlugins.getPluginUniqueId());
            jpaInstalledPlugins.setPluginVersion(installedPlugins.getVersion());
            installedPluginsDAO.insert(jpaInstalledPlugins);
            restWrapper = new RestWrapper(installedPlugins, RestWrapper.OK);
            LOGGER.info(RECORDWITHID + " inserted in AppDeploymentQueueStatus by User:" + principal.getName() );
        } catch (Exception e) {
            LOGGER.error( e);
            return new RestWrapper(e.getMessage(), RestWrapper.ERROR);
        }
        return restWrapper;
    }
    @Override
    public Object execute(String[] params) {
        return null;
    }

}