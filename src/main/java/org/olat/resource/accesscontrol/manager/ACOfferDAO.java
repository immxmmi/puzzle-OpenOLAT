/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */

package org.olat.resource.accesscontrol.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.TemporalType;
import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.OrganisationRef;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.CostCenter;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.model.OfferAndAccessInfos;
import org.olat.resource.accesscontrol.model.OfferImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Description:<br>
 *
 * <P>
 * Initial Date:  14 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service
public class ACOfferDAO {

	@Autowired
	private DB dbInstance;

	public List<Offer> findOfferByResource(OLATResource resource, boolean valid, Date atDate, boolean dateMandatory, Boolean webPublish, List<? extends OrganisationRef> organisations) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select offer, access");
		sb.append("  from acoffer offer");
		sb.append("  left join acofferaccess access")
		  .append("    on access.offer.key = offer.key ");
		sb.append("  left join fetch access.method accessMethod");
		sb.append("  left join offer.resource resource");
		if (organisations != null && !organisations.isEmpty()) {
			sb.append(" left join offertoorganisation oto");
			sb.append("   on oto.offer.key = offer.key");
		}
		sb.and().append("resource.key=:resourceKey");
		sb.and().append("offer.valid=").append(valid);
		if(atDate != null) {
			sb.and().append("(offer.validFrom is null or date(offer.validFrom)<=:atDate)");
			sb.and().append("(offer.validTo is null or date(offer.validTo)>=:atDate)");
		}
		if (dateMandatory) {
			sb.and().append("(offer.validFrom is not null or offer.validTo is not null)");
		}
		if (webPublish != null) {
			sb.and().append(" offer.catalogWebPublish =").append(webPublish);
		}
		if (organisations != null && !organisations.isEmpty()) {
			sb.and().append(" oto.organisation.key in :organisationKeys");
		}

		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("resourceKey", resource.getKey());
		if(atDate != null) {
			query.setParameter("atDate", atDate, TemporalType.DATE);
		}
		if (organisations != null && !organisations.isEmpty()) {
			query.setParameter("organisationKeys", organisations.stream().map(OrganisationRef::getKey).toList());
		}
		
		List<Object[]> loadedObjects = query.getResultList();
		// join to organisation may lead to duplicate offers
		Set<Offer> offers = new HashSet<>();
		for(Object[] objects:loadedObjects) {
			Offer offer = (Offer)objects[0];
			OfferAccess offerAccess = (OfferAccess)objects[1];
			if(offerAccess == null || offerAccess.getMethod().isVisibleInGui()) {
				offers.add(offer);
			}
		}
		
		return new ArrayList<>(offers);
	}
	
	public List<OfferAndAccessInfos> findOfferByResource(OLATResource resource, boolean valid) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select offer, access,");
		sb.append("  (select count(order.key) from acorder order")
		  .append("    inner join order.parts part")
		  .append("    inner join part.lines orderLine")
		  .append("    where orderLine.offer.key=offer.key")
		  .append("  ) as numOfOrders");
		sb.append("  from acoffer offer");
		sb.append("  left join acofferaccess access")
		  .append("    on access.offer.key = offer.key ");
		sb.append("  left join fetch access.method accessMethod");
		sb.append("  left join offer.resource resource");
		sb.and().append("resource.key=:resourceKey");
		sb.and().append("offer.valid=").append(valid);

		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("resourceKey", resource.getKey());

		List<Object[]> loadedObjects = query.getResultList();
		List<OfferAndAccessInfos> offers = new ArrayList<>(loadedObjects.size());
		for(Object[] objects:loadedObjects) {
			Offer offer = (Offer)objects[0];
			OfferAccess offerAccess = (OfferAccess)objects[1];
			int numOfOrders = PersistenceHelper.extractPrimitiveInt(objects, 2);
			if(offerAccess == null || offerAccess.getMethod().isVisibleInGui()) {
				offers.add(new OfferAndAccessInfos(offer, offerAccess, numOfOrders));
			}
			
		}
		return new ArrayList<>(offers);
	}

	public boolean isOpenAccessible(OLATResource olatResource, Boolean webPublish, List<? extends OrganisationRef> organisations) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select count(*)");
		sb.append("  from acoffer offer");
		sb.append(" inner join offertoorganisation oto");
		sb.append("   on oto.offer.key = offer.key");
		sb.and().append(" offer.valid = true");
		sb.and().append(" offer.openAccess = true");
		sb.and().append(" offer.resource.key=:resourceKey");
		if (webPublish != null) {
			sb.and().append(" offer.catalogWebPublish = ").append(webPublish);
		}
		if (organisations != null && !organisations.isEmpty()) {
			sb.and().append(" oto.organisation.key in :organisationKeys");
		}
		
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("resourceKey", olatResource.getKey());
		if (organisations != null && !organisations.isEmpty()) {
			query.setParameter("organisationKeys", organisations.stream().map(OrganisationRef::getKey).collect(Collectors.toList()));
		}
		
		return query.getSingleResult().longValue() > 0;
	}

	public List<OLATResource> loadOpenAccessibleResources(List<OLATResource> resources, Boolean webPublish, List<? extends OrganisationRef> organisations) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select distinct offer.resource");
		sb.append("  from acoffer offer");
		if (organisations != null && !organisations.isEmpty()) {
			sb.append(" inner join offertoorganisation oto");
			sb.append("   on oto.offer.key = offer.key");
		}
		sb.and().append(" offer.valid = true");
		sb.and().append(" offer.openAccess = true");
		sb.and().append(" offer.resource.key in :resourceKeys");
		if (webPublish != null) {
			sb.and().append(" offer.catalogWebPublish = ").append(webPublish);
		}
		if (organisations != null && !organisations.isEmpty()) {
			sb.and().append(" oto.organisation.key in :organisationKeys");
		}
		
		List<Long> resourceKeys = resources.stream().map(OLATResource::getKey).toList();
		TypedQuery<OLATResource> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), OLATResource.class)
				.setParameter("resourceKeys", resourceKeys);
		if (organisations != null && !organisations.isEmpty()) {
			query.setParameter("organisationKeys", organisations.stream().map(OrganisationRef::getKey).toList());
		}
		
		return query.getResultList();
	}
	
	public boolean isGuestAccessible(OLATResource olatResource) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select count(*)");
		sb.append("  from acoffer offer");
		sb.and().append(" offer.valid = true");
		sb.and().append(" offer.guestAccess = true");
		sb.and().append(" offer.resource.key=:resourceKey");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("resourceKey", olatResource.getKey())
				.getSingleResult().longValue() > 0;
	}
	
	public boolean offerExists(OLATResource olatResource) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select count(*)");
		sb.append("  from acoffer offer");
		sb.and().append(" offer.valid = true");
		sb.and().append(" offer.resource.key=:resourceKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("resourceKey", olatResource.getKey())
				.getSingleResult() > 0;
	}

	public List<OLATResource> loadGuestAccessibleResources(List<OLATResource> resources) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select distinct offer.resource");
		sb.append("  from acoffer offer");
		sb.and().append(" offer.valid = true");
		sb.and().append(" offer.guestAccess = true");
		sb.and().append(" offer.resource.key in :resourceKeys");
		
		List<Long> resourceKeys = resources.stream().map(OLATResource::getKey).toList();
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), OLATResource.class)
				.setParameter("resourceKeys", resourceKeys)
				.getResultList();
	}

	public Offer loadOfferByKey(Long key) {
		StringBuilder sb = new StringBuilder();
		sb.append("select offer from acoffer offer")
		  .append(" left join fetch offer.resource resource")
		  .append(" where offer.key=:offerKey");

		List<Offer> offers = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Offer.class)
				.setParameter("offerKey", key)
				.getResultList();
		if(offers.isEmpty()) return null;
		return offers.get(0);
	}

	public Offer createOffer(OLATResource resource, String resourceName) {
		OfferImpl offer = new OfferImpl();
		Date now = new Date();
		offer.setCreationDate(now);
		offer.setLastModified(now);
		offer.setResource(resource);
		offer.setValid(true);
		offer.setConfirmationEmail(false);
		offer.setCatalogPublish(true);
		offer.setCatalogWebPublish(false);
		if(resourceName != null && resourceName.length() > 255) {
			resourceName = resourceName.substring(0, 250);
		}
		offer.setResourceDisplayName(resourceName);
		offer.setResourceId(resource.getResourceableId());
		String resourceTypeName = resource.getResourceableTypeName();
		if(resourceTypeName != null && resourceTypeName.length() > 255) {
			resourceTypeName = resourceTypeName.substring(0, 250);
		}
		offer.setResourceTypeName(resourceTypeName);
		return offer;
	}
	
	public Offer copyAndPersistOffer(Offer offer, Date validFrom, Date validTo, OLATResource resource, String resourceName) {
		Offer offerCopy = createOffer(resource, resourceName);
		offerCopy.setAutoBooking(offer.isAutoBooking());
		offerCopy.setPrice(offer.getPrice());
		offerCopy.setCancellingFee(offer.getCancellingFee());
		offerCopy.setCancellingFeeDeadlineDays(offer.getCancellingFeeDeadlineDays());
		offerCopy.setCatalogPublish(offer.isCatalogPublish());
		offerCopy.setCatalogWebPublish(offer.isCatalogWebPublish());
		offerCopy.setConfirmationByManagerRequired(offer.isConfirmationByManagerRequired());
		offerCopy.setConfirmationEmail(offer.isConfirmationEmail());
		offerCopy.setCostCenter(offer.getCostCenter());
		offerCopy.setDescription(offer.getDescription());
		offerCopy.setGuestAccess(offer.isGuestAccess());
		offerCopy.setLabel(offer.getLabel());
		offerCopy.setOpenAccess(offer.isOpenAccess());
		offerCopy.setValidFrom(validFrom);
		offerCopy.setValidTo(validTo);
		dbInstance.getCurrentEntityManager().persist(offerCopy);
		return offerCopy;
	}

	public Offer deleteOffer(Offer offer) {
		offer = loadOfferByKey(offer.getKey());
		if(offer instanceof OfferImpl offerImpl) {
			offerImpl.setValid(false);
		}
		return saveOffer(offer);
	}

	public Offer saveOffer(Offer offer) {
		if(offer instanceof OfferImpl offerImpl) {
			offerImpl.setLastModified(new Date());
		}
		if(offer.getKey() == null) {
			dbInstance.getCurrentEntityManager().persist(offer);
		} else {
			offer = dbInstance.getCurrentEntityManager().merge(offer);
		}
		return offer;
	}
	
	public Offer save(Offer offer, CostCenter costCenter) {
		((OfferImpl)offer).setCostCenter(costCenter);
		return saveOffer(offer);
	}
	
	public Map<Long, Long> getCostCenterKeyToOfferCount(Collection<CostCenter> costCenters) {
		String query = """
				select offer.costCenter.key
				     , count(*)
				  from acoffer offer
				 where offer.costCenter.key in :costCenterKeys
				 group by offer.costCenter.key
				""";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, Object[].class)
				.setParameter("costCenterKeys", costCenters.stream().map(CostCenter::getKey).toList())
				.getResultList()
				.stream()
				.collect(Collectors.toMap(row -> (Long)row[0], row -> (Long)row[1]));
	}
	
}