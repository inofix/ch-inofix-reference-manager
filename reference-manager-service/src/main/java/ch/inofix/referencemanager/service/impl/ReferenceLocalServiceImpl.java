/**
 * Copyright (c) 2016-present Inofix GmbH, Luzern. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package ch.inofix.referencemanager.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jbibtex.BibTeXEntry;
import org.jbibtex.Key;
import org.jbibtex.StringValue;
import org.jbibtex.StringValue.Style;
import org.jbibtex.Value;

import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.asset.kernel.model.AssetLinkConstants;
import com.liferay.portal.kernel.backgroundtask.BackgroundTask;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskManager;
import com.liferay.portal.kernel.exception.LocaleException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.SystemEventConstants;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.Indexable;
import com.liferay.portal.kernel.search.IndexableType;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistryUtil;
import com.liferay.portal.kernel.search.QueryConfig;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.service.GroupLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.systemevent.SystemEvent;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.spring.extender.service.ServiceReference;

import ch.inofix.referencemanager.backgroundtask.ReferenceImportBackgroundTaskExecutor;
import ch.inofix.referencemanager.model.BibRefRelation;
import ch.inofix.referencemanager.model.Bibliography;
import ch.inofix.referencemanager.model.Reference;
import ch.inofix.referencemanager.service.base.ReferenceLocalServiceBaseImpl;
import ch.inofix.referencemanager.service.util.BibTeXUtil;
import ch.inofix.referencemanager.service.util.ReferenceImporter;
import ch.inofix.referencemanager.social.ReferenceActivityKeys;

/**
 * The implementation of the reference local service.
 *
 * <p>
 * All custom service methods should be put in this class. Whenever methods are
 * added, rerun ServiceBuilder to copy their definitions into the
 * {@link ch.inofix.referencemanager.service.ReferenceLocalService} interface.
 *
 * <p>
 * This is a local service. Methods of this service will not have security
 * checks based on the propagated JAAS credentials because this service can only
 * be accessed from within the same VM.
 * </p>
 *
 * @author Brian Wing Shun Chan
 * @author Christian Berndt
 * @created 2016-03-28 17:08
 * @modified 2017-10-29 17:27
 * @version 1.1.2
 * @see ReferenceLocalServiceBaseImpl
 * @see ch.inofix.referencemanager.service.ReferenceLocalServiceUtil
 */
public class ReferenceLocalServiceImpl extends ReferenceLocalServiceBaseImpl {
    /*
     * NOTE FOR DEVELOPERS:
     * 
     * Never reference this class directly. Always use {@link
     * ch.inofix.referencemanager.service.ReferenceLocalServiceUtil} to access
     * the reference local service.
     */

    @Indexable(type = IndexableType.REINDEX)
    @Override
    public Reference addReference(long userId, String bibTeX, long[] bibliographyIds, ServiceContext serviceContext)
            throws PortalException {

        // Reference

        User user = userPersistence.findByPrimaryKey(userId);

        long groupId = serviceContext.getScopeGroupId();

        long companyId = user.getCompanyId();

        long referenceId = counterLocalService.increment();

        Reference reference = referencePersistence.create(referenceId);

        reference.setUuid(serviceContext.getUuid());
        reference.setGroupId(groupId);
        reference.setCompanyId(companyId);
        reference.setUserId(user.getUserId());
        reference.setUserName(user.getFullName());
        reference.setExpandoBridgeAttributes(serviceContext);

        BibTeXEntry bibTeXEntry = BibTeXUtil.getBibTeXEntry(bibTeX);
        if (bibTeXEntry != null) {
            Key key = new Key("bibshare-id");
            Value value = new StringValue(String.valueOf(referenceId), Style.QUOTED);
            bibTeXEntry.addField(key, value);
        } else {
            // TODO: raise an error and report to the user that something is
            // wrong with the bibtex-src.
        }

        bibTeX = BibTeXUtil.format(bibTeXEntry);

        reference.setBibTeX(bibTeX);

        referencePersistence.update(reference);

//        // Match user and group references against common references
//
//        long defaultGroupId = GetterUtil.getLong(PropsUtil.get("default.group.id"));
//
//        if (reference.getGroupId() != defaultGroupId) {
//            
//            match(reference);
//        }

        // BibRefRelation

        for (long bibliographyId : bibliographyIds) {

            Bibliography bibliography = bibliographyLocalService.getBibliography(bibliographyId);

            long bibRefRelationId = counterLocalService.increment();
            BibRefRelation bibRefRelation = bibRefRelationPersistence.create(bibRefRelationId);

            bibRefRelation.setGroupId(bibliography.getGroupId());
            bibRefRelation.setCompanyId(bibliography.getCompanyId());
            bibRefRelation.setUserId(bibliography.getUserId());
            bibRefRelation.setUserName(bibliography.getUserName());

            bibRefRelation.setBibliographyId(bibliography.getBibliographyId());
            bibRefRelation.setReferenceId(referenceId);

            bibRefRelationPersistence.update(bibRefRelation);

        }

        // Resources

        resourceLocalService.addModelResources(reference, serviceContext);

        // Asset

        updateAsset(userId, reference, serviceContext.getAssetCategoryIds(), serviceContext.getAssetTagNames(),
                serviceContext.getAssetLinkEntryIds(), serviceContext.getAssetPriority());

        // Social

        JSONObject extraDataJSONObject = JSONFactoryUtil.createJSONObject();

        extraDataJSONObject.put("title", reference.getCitation());

        socialActivityLocalService.addActivity(userId, groupId, Reference.class.getName(), referenceId,
                ReferenceActivityKeys.ADD_REFERENCE, extraDataJSONObject.toString(), 0);

        return reference;

    }

    @Override
    public Reference addReference(long userId, String bibTeX, ServiceContext serviceContext) throws PortalException {

        return addReference(userId, bibTeX, new long[0], serviceContext);

    }

    @Override
    public void deleteReferences(long groupId) throws PortalException {
        for (Reference reference : referencePersistence.findByGroupId(groupId)) {
            referenceLocalService.deleteReference(reference);
        }
    }

    @Indexable(type = IndexableType.DELETE)
    @Override
    @SystemEvent(type = SystemEventConstants.TYPE_DELETE)
    public Reference deleteReference(Reference reference) throws PortalException {

        // Reference

        referencePersistence.remove(reference);

        // Resources

        resourceLocalService.deleteResource(reference.getCompanyId(), Reference.class.getName(),
                ResourceConstants.SCOPE_INDIVIDUAL, reference.getReferenceId());

        // Subscriptions

        subscriptionLocalService.deleteSubscriptions(reference.getCompanyId(), Reference.class.getName(),
                reference.getReferenceId());

        // Asset

        assetEntryLocalService.deleteEntry(Reference.class.getName(), reference.getReferenceId());

        // Comment

        // TODO
        // deleteDiscussion(reference);

        // Expando

        expandoRowLocalService.deleteRows(reference.getReferenceId());

        // Ratings

        ratingsStatsLocalService.deleteStats(Reference.class.getName(), reference.getReferenceId());

        // Trash

        trashEntryLocalService.deleteEntry(Reference.class.getName(), reference.getReferenceId());

        // Workflow

        // TODO: do we need workflow support?
        // workflowInstanceLinkLocalService.deleteWorkflowInstanceLinks(
        // reference.getCompanyId(), reference.getGroupId(),
        // Reference.class.getName(), reference.getReferenceId());

        return reference;
    }

    @Override
    public Reference deleteReference(long referenceId) throws PortalException {
        Reference reference = referencePersistence.findByPrimaryKey(referenceId);

        bibRefRelationLocalService.deleteByReferenceId(referenceId);

        refRefRelationLocalService.deleteByReferenceId(referenceId);

        return referenceLocalService.deleteReference(reference);
    }

    public List<Reference> getGroupReferences(long groupId) throws PortalException, SystemException {

        return referencePersistence.findByGroupId(groupId);
    }

    @Override
    public Reference getReference(long referenceId) throws PortalException {
        return referencePersistence.findByPrimaryKey(referenceId);
    }

    /**
     * Imports the references from the file.
     *
     * @param userId
     *            the primary key of the user
     * @param groupId
     *            the primary key of the group
     * @param privateLayout
     *            whether the layout is private to the group
     * @param parameterMap
     *            the mapping of parameters indicating which information will be
     *            imported.
     * @param file
     *            the file with the data
     * @param serviceContext
     * @since 1.0.2
     * @throws PortalException
     *             if a group or user with the primary key could not be found,
     *             or if some other portal exception occurred
     * @throws SystemException
     *             if a system exception occurred
     * @see com.liferay.portal.lar.LayoutImporter
     */
    public void importReferences(long userId, long groupId, boolean privateLayout, Map<String, String[]> parameterMap,
            File file, ServiceContext serviceContext) throws PortalException, SystemException {

        try {
            ReferenceImporter referenceImporter = new ReferenceImporter();

            referenceImporter.importReferences(userId, groupId, privateLayout, parameterMap, file, serviceContext);

        } catch (PortalException pe) {
            Throwable cause = pe.getCause();

            if (cause instanceof LocaleException) {
                throw (PortalException) cause;
            }

            _log.error(pe);

            throw pe;
        } catch (SystemException se) {

            _log.error(se);

            throw se;
        } catch (Exception e) {

            _log.error(e);

            throw new SystemException(e);
        }
    }

    /**
     * Imports the references from the input stream.
     *
     * @param userId
     *            the primary key of the user
     * @param groupId
     *            the primary key of the group
     * @param privateLayout
     *            whether the layout is private to the group
     * @param parameterMap
     *            the mapping of parameters indicating which information will be
     *            imported.
     * @param inputStream
     *            the input stream
     * @param serviceContext
     * @since 1.0.2
     * @throws PortalException
     *             if a group or user with the primary key could not be found,
     *             or if some other portal exception occurred
     * @throws SystemException
     *             if a system exception occurred
     */
    public void importReferences(long userId, long groupId, boolean privateLayout, Map<String, String[]> parameterMap,
            InputStream inputStream, ServiceContext serviceContext) throws PortalException, SystemException {

        File file = null;

        try {
            file = FileUtil.createTempFile("bib");

            FileUtil.write(file, inputStream);

            importReferences(userId, groupId, privateLayout, parameterMap, file, serviceContext);

        } catch (IOException ioe) {

            _log.error(ioe);

            throw new SystemException(ioe);
        } finally {
            FileUtil.delete(file);
        }
    }

    /**
     * 
     * @param userId
     * @param taskName
     * @param groupId
     * @param privateLayout
     * @param parameterMap
     * @param file
     * @param serviceContext
     * @since 1.0.8
     * @return
     * @throws PortalException
     */
    public long importReferencesInBackground(long userId, String taskName, long groupId, boolean privateLayout,
            Map<String, String[]> parameterMap, File file, ServiceContext serviceContext) throws PortalException {

        Map<String, Serializable> taskContextMap = new HashMap<String, Serializable>();
        taskContextMap.put("userId", userId);
        taskContextMap.put("groupId", groupId);
        taskContextMap.put("parameterMap", (Serializable) parameterMap);
        taskContextMap.put("privateLayout", privateLayout);
        taskContextMap.put("serviceContext", serviceContext);

        BackgroundTask backgroundTask = backgroundTaskmanager.addBackgroundTask(userId, groupId, taskName,
                ReferenceImportBackgroundTaskExecutor.class.getName(), taskContextMap, serviceContext);

        backgroundTask.addAttachment(userId, file.getName(), file);

        return backgroundTask.getBackgroundTaskId();

    }

    public void match(Reference reference) throws PortalException {

        _log.info("match");

        long defaultGroupId = GetterUtil.getLong(PropsUtil.get("default.group.id"));

        Hits hits = search(reference.getUserId(), defaultGroupId, -1, null, reference.getTitle(), null,
                WorkflowConstants.STATUS_ANY, null, false, 0, 20, null);

        _log.info("hits.getLength() = " + hits.getLength());

        if (hits.getLength() == 0) {

            // not yet in the pool of common references

            _log.info("not yet in the global references ");

            // TODO: strip the private fields from the reference
            String bibTeX = reference.getBibTeX();

            ServiceContext serviceContext = new ServiceContext();
            serviceContext.setScopeGroupId(defaultGroupId);

            Reference globalReference = addReference(reference.getUserId(), bibTeX, serviceContext);

            refRefRelationLocalService.addRefRefRelation(reference.getUserId(), globalReference.getReferenceId(),
                    reference.getReferenceId(), new ServiceContext());
        }
    }

    @Indexable(type = IndexableType.REINDEX)
    public Reference reIndexReference(long referenceId) throws PortalException {
        return getReference(referenceId);
    }

    @Override
    public Hits search(long userId, long groupId, long bibliographyId, String keywords, int start, int end, Sort sort)
            throws PortalException {

        if (sort == null) {
            sort = new Sort(Field.MODIFIED_DATE, true);
        }

        String author = null;
        String title = null;
        String year = null;

        boolean andOperator = false;

        if (Validator.isNotNull(keywords)) {

            author = keywords;
            title = keywords;
            year = keywords;

        } else {
            andOperator = true;
        }

        return search(userId, groupId, bibliographyId, author, title, year, WorkflowConstants.STATUS_ANY, null, andOperator, start, end,
                sort);

    }

    @Override
    public Hits search(long userId, long groupId, long bibliographyId, String author, String title, String year, int status,
            LinkedHashMap<String, Object> params, boolean andSearch, int start, int end, Sort sort)
            throws PortalException {

        if (sort == null) {
            sort = new Sort(Field.MODIFIED_DATE, true);
        }

        Indexer<Reference> indexer = IndexerRegistryUtil.getIndexer(Reference.class.getName());

        SearchContext searchContext = buildSearchContext(userId, groupId, bibliographyId, author, title, year, status, params,
                andSearch, start, end, sort);

        return indexer.search(searchContext);
    }

    @Override
    public void subscribe(long userId, long groupId) throws PortalException {
        subscriptionLocalService.addSubscription(userId, groupId, Reference.class.getName(), groupId);
    }

    @Override
    public void unsubscribe(long userId, long groupId) throws PortalException {
        subscriptionLocalService.deleteSubscription(userId, Reference.class.getName(), groupId);
    }

    public void updateAsset(long userId, Reference reference, long[] assetCategoryIds, String[] assetTagNames,
            long[] assetLinkEntryIds, Double priority) throws PortalException {

        boolean visible = false;

        Date publishDate = null;

        if (reference.isApproved()) {
            visible = true;

            publishDate = reference.getCreateDate();
        }

        String summary = HtmlUtil.extractText(StringUtil.shorten(reference.getCitation(), 500));

        AssetEntry assetEntry = assetEntryLocalService.updateEntry(userId, reference.getGroupId(),
                reference.getCreateDate(), reference.getModifiedDate(), Reference.class.getName(),
                reference.getReferenceId(), reference.getUuid(), 0, assetCategoryIds, assetTagNames, true, visible,
                null, null, publishDate, null, ContentTypes.TEXT_HTML, reference.getTitle(), reference.getCitation(),
                summary, null, null, 0, 0, priority);

        assetLinkLocalService.updateLinks(userId, assetEntry.getEntryId(), assetLinkEntryIds,
                AssetLinkConstants.TYPE_RELATED);
    }

    public Reference updateReference(long referenceId, long userId, String bibTeX, ServiceContext serviceContext)
            throws PortalException {
        return updateReference(referenceId, userId, bibTeX, new long[0], serviceContext);
    }

    @Indexable(type = IndexableType.REINDEX)
    public Reference updateReference(long referenceId, long userId, String bibTeX, long[] bibliographyIds,
            ServiceContext serviceContext) throws PortalException {

        _log.info("updateReference");

        // Reference

        User user = userPersistence.findByPrimaryKey(userId);

        Reference reference = referencePersistence.findByPrimaryKey(referenceId);
        long groupId = serviceContext.getScopeGroupId();
        long companyId = user.getCompanyId();

        reference.setUuid(serviceContext.getUuid());
        reference.setGroupId(groupId);
        reference.setCompanyId(companyId);
        reference.setUserId(user.getUserId());
        reference.setUserName(user.getFullName());
        reference.setExpandoBridgeAttributes(serviceContext);

        BibTeXEntry bibTeXEntry = BibTeXUtil.getBibTeXEntry(bibTeX);
        if (bibTeXEntry != null) {
            Key key = new Key("bibshare-last-modified");
            Value value = new StringValue(String.valueOf(new Date().getTime()), Style.QUOTED);
            bibTeXEntry.addField(key, value);
        } else {
            // TODO: raise an error and report to the user that something is
            // wrong with the bibtex-src.
        }

        bibTeX = BibTeXUtil.format(bibTeXEntry);

        reference.setBibTeX(bibTeX);

        reference = referencePersistence.update(reference);

        // Match user and group references against common references

        long defaultGroupId = GetterUtil.getLong(PropsUtil.get("default.group.id"));

        if (reference.getGroupId() != defaultGroupId) {
            match(reference);
        }

        // BibRefRelation

        for (long bibliographyId : bibliographyIds) {

            Bibliography bibliography = bibliographyLocalService.getBibliography(bibliographyId);

            long bibRefRelationId = counterLocalService.increment();
            BibRefRelation bibRefRelation = bibRefRelationPersistence.create(bibRefRelationId);

            bibRefRelation.setGroupId(bibliography.getGroupId());
            bibRefRelation.setCompanyId(bibliography.getCompanyId());
            bibRefRelation.setUserId(bibliography.getUserId());
            bibRefRelation.setUserName(bibliography.getUserName());

            bibRefRelation.setBibliographyId(bibliography.getBibliographyId());
            bibRefRelation.setReferenceId(referenceId);

            bibRefRelationPersistence.update(bibRefRelation);

        }

        // Resources

        resourceLocalService.addModelResources(reference, serviceContext);

        // Asset

        updateAsset(userId, reference, serviceContext.getAssetCategoryIds(), serviceContext.getAssetTagNames(),
                serviceContext.getAssetLinkEntryIds(), serviceContext.getAssetPriority());

        // Social

        JSONObject extraDataJSONObject = JSONFactoryUtil.createJSONObject();

        extraDataJSONObject.put("title", reference.getCitation());

        socialActivityLocalService.addActivity(userId, groupId, Reference.class.getName(), referenceId,
                ReferenceActivityKeys.UPDATE_REFERENCE, extraDataJSONObject.toString(), 0);

        return reference;

    }

    protected SearchContext buildSearchContext(long userId, long groupId, long bibliographyId, String author, String title, String year,
            int status, LinkedHashMap<String, Object> params, boolean andSearch, int start, int end, Sort sort)
            throws PortalException {
        
        SearchContext searchContext = new SearchContext();

        searchContext.setAttribute(Field.STATUS, status);

        if (Validator.isNotNull(author)) {
            searchContext.setAttribute("author", author);
        }
        
        if (bibliographyId > 0) {
            searchContext.setAttribute("bibliographyId", bibliographyId);
        }

        if (Validator.isNotNull(title)) {
            searchContext.setAttribute("title", title);
        }

        if (Validator.isNotNull(year)) {
            searchContext.setAttribute("year", year);
        }

        searchContext.setAttribute("paginationType", "more");

        Group group = GroupLocalServiceUtil.getGroup(groupId);

        searchContext.setCompanyId(group.getCompanyId());

        searchContext.setEnd(end);
        if (groupId > 0) {
            searchContext.setGroupIds(new long[] { groupId });
        }
        searchContext.setSorts(sort);
        searchContext.setStart(start);
        searchContext.setUserId(userId);

        searchContext.setAndSearch(andSearch);

        if (params != null) {

            String keywords = (String) params.remove("keywords");

            if (Validator.isNotNull(keywords)) {
                searchContext.setKeywords(keywords);
            }
        }

        QueryConfig queryConfig = new QueryConfig();

        queryConfig.setHighlightEnabled(false);
        queryConfig.setScoreEnabled(false);

        searchContext.setQueryConfig(queryConfig);

        if (sort != null) {
            searchContext.setSorts(sort);
        }

        searchContext.setStart(start);

        return searchContext;
    }

    @ServiceReference(type = BackgroundTaskManager.class)
    protected BackgroundTaskManager backgroundTaskmanager;

    private static final Log _log = LogFactoryUtil.getLog(ReferenceLocalServiceImpl.class);

}