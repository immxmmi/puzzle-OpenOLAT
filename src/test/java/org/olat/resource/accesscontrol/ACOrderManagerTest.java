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

package org.olat.resource.accesscontrol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.DateUtils;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceImpl;
import org.olat.resource.OLATResourceManager;
import org.olat.resource.accesscontrol.manager.ACMethodDAO;
import org.olat.resource.accesscontrol.manager.ACOrderDAO;
import org.olat.resource.accesscontrol.manager.ACTransactionDAO;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.AccessTransactionImpl;
import org.olat.resource.accesscontrol.model.AccessTransactionStatus;
import org.olat.resource.accesscontrol.model.FreeAccessMethod;
import org.olat.resource.accesscontrol.model.OrderImpl;
import org.olat.resource.accesscontrol.model.PriceImpl;
import org.olat.resource.accesscontrol.model.RawOrderItem;
import org.olat.resource.accesscontrol.model.TokenAccessMethod;
import org.olat.resource.accesscontrol.ui.OrdersDataModel;
import org.olat.resource.accesscontrol.ui.OrdersDataModel.OrderCol;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * test the order manager
 * 
 * <P>
 * Initial Date:  19 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ACOrderManagerTest extends OlatTestCase {
	
	private static Identity ident1, ident2, ident3;
	private static Identity ident4, ident5, ident6;
	private static Identity ident7, ident8;
	private static boolean isInitialized = false;
	
	@Autowired
	private DB dbInstance;
	
	@Autowired
	private ACTransactionDAO acTransactionManager;
	@Autowired
	private ACService acService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private ACOrderDAO acOrderManager;
	@Autowired
	private ACMethodDAO acMethodManager;
	@Autowired
	private OLATResourceManager resourceManager;
	
	@Before
	public void setUp() {
		if(!isInitialized) {
			ident1 = JunitTestHelper.createAndPersistIdentityAsRndUser("order-1");
			ident2 = JunitTestHelper.createAndPersistIdentityAsRndUser("order-2");
			ident3 = JunitTestHelper.createAndPersistIdentityAsRndUser("order-3");
			ident4 = JunitTestHelper.createAndPersistIdentityAsRndUser("order-4");
			ident5 = JunitTestHelper.createAndPersistIdentityAsRndUser("order-5");
			ident6 = JunitTestHelper.createAndPersistIdentityAsRndUser("order-6");
			ident7 = JunitTestHelper.createAndPersistIdentityAsRndUser("order-7");
			ident8 = JunitTestHelper.createAndPersistIdentityAsRndUser("order-8");
		}
	}
	
	@Test
	public void testManagers() {
		assertNotNull(acService);
		assertNotNull(dbInstance);
		assertNotNull(acMethodManager);
		assertNotNull(acOrderManager);
	}
	
	@Test
	public void saveOrder() {
		//create an offer to buy
		OLATResource randomOres = createResource();
		Offer offer = acService.createOffer(randomOres, "TestSaveOrder");
		offer = acService.save(offer);
		
		dbInstance.commitAndCloseSession();
		
		//create and save an order
		Order order = acOrderManager.createOrder(ident1);
		OrderPart part = acOrderManager.addOrderPart(order);
		OrderLine item = acOrderManager.addOrderLine(part, offer);
		assertNotNull(order);
		assertNotNull(order.getDelivery());
		assertEquals(ident1, order.getDelivery());
		acOrderManager.save(order);
		dbInstance.commitAndCloseSession();
		
		order = acOrderManager.loadOrderByKey(order.getKey());
		BillingAddress billingAddress = acService.createBillingAddress(JunitTestHelper.getDefaultOrganisation(), null);
		acService.addBillingAddress(order, billingAddress);
		dbInstance.commitAndCloseSession();
		
		//check what's on DB
		Order retrievedOrder = acOrderManager.loadOrderByKey(order.getKey());
		assertNotNull(retrievedOrder);
		assertNotNull(retrievedOrder.getDelivery());
		assertEquals(ident1, retrievedOrder.getDelivery());
		assertEquals(order, retrievedOrder);
		
		List<OrderPart> parts = retrievedOrder.getParts();
		assertNotNull(parts);
		assertEquals(1, parts.size());
		assertEquals(part, parts.get(0));
		
		OrderPart retrievedPart = parts.get(0);
		assertNotNull(retrievedPart.getOrderLines());
		assertEquals(1, retrievedPart.getOrderLines().size());
		assertEquals(item, retrievedPart.getOrderLines().get(0));
		
		OrderLine retrievedItem = retrievedPart.getOrderLines().get(0);
		assertNotNull(retrievedItem.getOffer());
		assertEquals(offer, retrievedItem.getOffer());
		
		assertEquals(order.getBillingAddress(), billingAddress);
	}
	
	@Test
	public void saveOrderWithStatus() {
		//create an offer to buy
		OLATResource randomOres = createResource();
		Offer offer = acService.createOffer(randomOres, "TestSaveOrder");
		offer = acService.save(offer);
		
		dbInstance.commitAndCloseSession();
		
		//create and save an order
		Order order = acOrderManager.createOrder(ident1);
		OrderPart part = acOrderManager.addOrderPart(order);
		OrderLine item = acOrderManager.addOrderLine(part, offer);
		Assert.assertNotNull(order);
		Assert.assertNotNull(item);
		Assert.assertNotNull(order.getDelivery());
		Assert.assertEquals(ident1, order.getDelivery());
		acOrderManager.save(order, OrderStatus.PAYED);
		dbInstance.commitAndCloseSession();
		
		//check what's on DB
		Order retrievedOrder = acOrderManager.loadOrderByKey(order.getKey());
		Assert.assertNotNull(retrievedOrder);
		Assert.assertEquals(ident1, retrievedOrder.getDelivery());
		Assert.assertEquals(order, retrievedOrder);
		Assert.assertEquals(OrderStatus.PAYED, retrievedOrder.getOrderStatus());
	}
	
	@Test
	public void saveOrderWithCancellationFee() {
		//create an offer to buy
		OLATResource randomOres = createResource();
		Offer offer = acService.createOffer(randomOres, "TestSaveOrderToCancel");
		offer.setPrice(new PriceImpl(BigDecimal.valueOf(10.0), "CHF"));
		offer.setCancellingFee(new PriceImpl(BigDecimal.valueOf(2.0), "CHF"));
		offer = acService.save(offer);
		
		dbInstance.commitAndCloseSession();
		
		//create and save an order
		Order order = acOrderManager.createOrder(ident1);
		OrderPart part = acOrderManager.addOrderPart(order);
		OrderLine item = acOrderManager.addOrderLine(part, offer);
		assertNotNull(order);
		assertNotNull(order.getDelivery());
		assertEquals(ident1, order.getDelivery());
		order.recalculate();
		acOrderManager.save(order);
		dbInstance.commitAndCloseSession();
		
		order = acOrderManager.loadOrderByKey(order.getKey());
		BillingAddress billingAddress = acService.createBillingAddress(JunitTestHelper.getDefaultOrganisation(), null);
		acService.addBillingAddress(order, billingAddress);
		dbInstance.commitAndCloseSession();
		
		// Check the total on the database
		Order retrievedOrder = acOrderManager.loadOrderByKey(order.getKey());
		Assert.assertNotNull(retrievedOrder);
		Assert.assertEquals(ident1, retrievedOrder.getDelivery());
		Assert.assertEquals(order, retrievedOrder);
		Assert.assertEquals(billingAddress, retrievedOrder.getBillingAddress());
		Assertions.assertThat(retrievedOrder.getTotal().getAmount()).isEqualByComparingTo(BigDecimal.valueOf(10.0));
		// Cancellation will be set only if cancelled
		Assert.assertNull(retrievedOrder.getCancellationFees());

		// Order part
		List<OrderPart> parts = retrievedOrder.getParts();
		Assert.assertNotNull(parts);
		Assert.assertEquals(1, parts.size());
		Assert.assertEquals(part, parts.get(0));
		
		OrderPart retrievedPart = parts.get(0);
		Assert.assertNotNull(retrievedPart.getOrderLines());
		Assert.assertEquals(1, retrievedPart.getOrderLines().size());
		Assert.assertEquals(item, retrievedPart.getOrderLines().get(0));
		Assertions.assertThat(retrievedPart.getTotal().getAmount()).isEqualByComparingTo(BigDecimal.valueOf(10.0));
		Assertions.assertThat(retrievedPart.getCancellationFees().getAmount()).isEqualByComparingTo(BigDecimal.valueOf(2.0));
		
		// Order line
		OrderLine retrievedItem = retrievedPart.getOrderLines().get(0);
		Assert.assertNotNull(retrievedItem.getOffer());
		Assert.assertEquals(offer, retrievedItem.getOffer());
		Assertions.assertThat(retrievedItem.getTotal().getAmount()).isEqualByComparingTo(BigDecimal.valueOf(10.0));
		Assertions.assertThat(retrievedItem.getCancellationFee().getAmount()).isEqualByComparingTo(BigDecimal.valueOf(2.0));
	}
	
	@Test
	public void loadOrderByKey() {
		//create an offer to buy
		OLATResource randomOres = createResource();
		Offer offer = acService.createOffer(randomOres, "loadOrderByKey");
		offer = acService.save(offer);
		dbInstance.commit();
		
		//create and save an order
		Order order = acOrderManager.createOrder(ident1);
		OrderPart part = acOrderManager.addOrderPart(order);
		acOrderManager.addOrderLine(part, offer);
		order = acOrderManager.save(order);
		dbInstance.commitAndCloseSession();
		
		Order reloadedOrder = acOrderManager.loadOrderByKey(order.getKey());
		Assert.assertNotNull(reloadedOrder);
		Assert.assertEquals(order, reloadedOrder);	
	}
	
	@Test
	public void loadOrderByNr() {
		//create an offer to buy
		OLATResource randomOres = createResource();
		Offer offer = acService.createOffer(randomOres, "loadOrderByNr");
		offer = acService.save(offer);
		dbInstance.commit();
		
		//create and save an order
		Order order = acOrderManager.createOrder(ident1);
		OrderPart part = acOrderManager.addOrderPart(order);
		acOrderManager.addOrderLine(part, offer);
		order = acOrderManager.save(order);
		dbInstance.commitAndCloseSession();
		
		Order reloadedOrder = acOrderManager.loadOrderByNr(order.getKey().toString());
		Assert.assertNotNull(reloadedOrder);
		Assert.assertEquals(order, reloadedOrder);	
	}
	
	@Test
	public void findOrderItemsOfResource() {
		//create an offer to buy
		List<AccessMethod> methods = acMethodManager.getAvailableMethodsByType(TokenAccessMethod.class);
		assertNotNull(methods);
		assertEquals(1, methods.size());
		AccessMethod tokenMethod = methods.get(0);
		
		OLATResource randomOres = createResource();
		Offer offer = acService.createOffer(randomOres, "TestSaveOrder");
		offer = acService.save(offer);
		
		dbInstance.commitAndCloseSession();
		
		//create and save an order
		Order order = acOrderManager.createOrder(ident1);
		OrderPart part = acOrderManager.addOrderPart(order);
		OrderLine line = acOrderManager.addOrderLine(part, offer);
		order = acOrderManager.save(order);
		Assert.assertNotNull(order);
		Assert.assertNotNull(order.getDelivery());
		Assert.assertNotNull(line);
		Assert.assertEquals(ident1, order.getDelivery());
		
		dbInstance.commitAndCloseSession();
		
		AccessTransaction accessTransaction = acTransactionManager.createTransaction(order, part, tokenMethod);
		assertNotNull(accessTransaction);
		acTransactionManager.save(accessTransaction);

		AccessTransaction accessTransaction2 = acTransactionManager.createTransaction(order, part, tokenMethod);
		assertNotNull(accessTransaction2);
		acTransactionManager.save(accessTransaction2);

		dbInstance.commitAndCloseSession();
		acTransactionManager.update(accessTransaction, AccessTransactionStatus.NEW);
		acTransactionManager.update(accessTransaction2, AccessTransactionStatus.CANCELED);

		long start = System.nanoTime();
		List<RawOrderItem> items = acOrderManager.findNativeOrderItems(randomOres, null, null, null, null, null, null,
				null, false, false, 0, -1, null);
		CodeHelper.printMilliSecondTime(start, "Order itemized");
		Assert.assertNotNull(items);
		
		//check the order by
		for(OrderCol col:OrderCol.values()) {
			if(col.sortable()) {
				List<RawOrderItem> rawItems = acOrderManager.findNativeOrderItems(randomOres, null, null, null, null, null, null,
						null, false, false, 0, -1, null, new SortKey(col.sortKey(), false));
				Assert.assertNotNull(rawItems);
			}
		}
	}
	
	@Test
	public void findOrderItems() {
		//create an offer to buy

		OLATResource randomOres = createResource();
		Offer offer = acService.createOffer(randomOres, "TestSaveOrder");
		offer = acService.save(offer);
		
		dbInstance.commitAndCloseSession();
		
		//create and save an order
		Order order = acOrderManager.createOrder(ident1);
		OrderPart part = acOrderManager.addOrderPart(order);
		OrderLine line = acOrderManager.addOrderLine(part, offer);
		order = acOrderManager.save(order);
		Assert.assertNotNull(order);
		Assert.assertNotNull(order.getDelivery());
		Assert.assertNotNull(line);
		Assert.assertEquals(ident1, order.getDelivery());

		dbInstance.commitAndCloseSession();
		
		long start = System.nanoTime();
		List<RawOrderItem> items = acOrderManager.findNativeOrderItems(randomOres, null, null, null, null, null, null,
				null, false, false, 0, -1, null);
		CodeHelper.printMilliSecondTime(start, "Order itemized");
		Assert.assertNotNull(items);
	}
	
	@Test
	public void findOrderItemsAllParameters() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("Find-1");
		OLATResource randomOres = createResource();
		Offer offer = acService.createOffer(randomOres, "Find orders");
		offer = acService.save(offer);
		
		dbInstance.commitAndCloseSession();
		
		//create and save an order
		Order orderToSave = acOrderManager.createOrder(id);
		OrderPart part = acOrderManager.addOrderPart(orderToSave);
		OrderLine line = acOrderManager.addOrderLine(part, offer);
		final Order order = acOrderManager.save(orderToSave);
		Assert.assertNotNull(order);
		Assert.assertNotNull(order.getDelivery());
		Assert.assertNotNull(line);
		Assert.assertEquals(id, order.getDelivery());

		dbInstance.commitAndCloseSession();
		
		List<UserPropertyHandler> userPropertyHandlers = userManager
				.getUserPropertyHandlersFor(OrdersDataModel.class.getCanonicalName(), true);
		List<RawOrderItem> items = acOrderManager.findNativeOrderItems(randomOres, null, order.getKey(),
				DateUtils.addDays(new Date(), -2), DateUtils.addDays(new Date(), 2),
				OrderStatus.values(), null, null, false, false,
				0, 256, userPropertyHandlers);
		
		Assertions.assertThat(items)
			.hasSize(1)
			.allMatch(item -> order.getKey().equals(item.getOrderKey()));
		
		// Syntax check only
		acOrderManager.findNativeOrderItems(randomOres, null, order.getKey(),
				DateUtils.addDays(new Date(), -2), DateUtils.addDays(new Date(), 2),
				OrderStatus.values(), null, null, true, true,
				0, 256, userPropertyHandlers);
	}
	
	@Test
	public void saveOneClickOrders() {
		//create some offers to buy
		OLATResource randomOres1 = createResource();
		Offer offer1 = acService.createOffer(randomOres1, "TestSaveOneClickOrders 1");
		offer1 = acService.save(offer1);
		
		OLATResource randomOres2 = createResource();
		Offer offer2 = acService.createOffer(randomOres2, "TestSaveOneClickOrders 2");
		offer2 = acService.save(offer2);
		
		dbInstance.commitAndCloseSession();
		
		//create a link offer to method
		List<AccessMethod> methods = acMethodManager.getAvailableMethodsByType(TokenAccessMethod.class);
		assertNotNull(methods);
		assertEquals(1, methods.size());
		AccessMethod method = methods.get(0);
		
		OfferAccess access1 = acMethodManager.createOfferAccess(offer1, method);
		acMethodManager.save(access1);
		
		OfferAccess access2 = acMethodManager.createOfferAccess(offer2, method);
		acMethodManager.save(access2);

		dbInstance.commitAndCloseSession();
		
		//one clicks
		acOrderManager.saveOneClick(ident1, access1);
		acOrderManager.saveOneClick(ident2, access2);
		acOrderManager.saveOneClick(ident3, access1);
		acOrderManager.saveOneClick(ident3, access2);
		
		dbInstance.commitAndCloseSession();
		
		//retrieves by identity
		List<Order> ordersIdent3 = acOrderManager.findOrdersByDelivery(ident3);
		assertEquals(2, ordersIdent3.size());
		assertEquals(ident3, ordersIdent3.get(0).getDelivery());
		assertEquals(ident3, ordersIdent3.get(1).getDelivery());
		
		//retrieves by resource
		List<Order> ordersResource2 = acOrderManager.findOrdersByResource(randomOres2);
		assertEquals(2, ordersResource2.size());
	}
	
	@Test
	public void saveOneClickOrder() {
	//make extensiv test on one order
		//create some offers to buy
		OLATResource randomOres1 = createResource();
		Offer offer = acService.createOffer(randomOres1, "TestSaveOneClickOrder 1");
		offer = acService.save(offer);

		dbInstance.commitAndCloseSession();
		
		//create a link offer to method
		List<AccessMethod> methods = acMethodManager.getAvailableMethodsByType(FreeAccessMethod.class);
		Assert.assertNotNull(methods);
		Assert.assertEquals(1, methods.size());
		AccessMethod method = methods.get(0);
		
		OfferAccess access1 = acMethodManager.createOfferAccess(offer, method);
		acMethodManager.save(access1);

		dbInstance.commitAndCloseSession();
		
		//one clicks
		Order order = acOrderManager.saveOneClick(ident7, access1);
		
		dbInstance.commitAndCloseSession();
		
		//load order
		Order retrivedOrder = acOrderManager.loadOrderByKey(order.getKey());
		
		Assert.assertNotNull(retrivedOrder);
		Assert.assertNotNull(retrivedOrder.getCreationDate());
		Assert.assertNotNull(retrivedOrder.getDelivery());
		Assert.assertNotNull(retrivedOrder.getOrderNr());
		Assert.assertNotNull(retrivedOrder.getParts());
		
		Assert.assertEquals(ident7, retrivedOrder.getDelivery());
		Assert.assertEquals(1, retrivedOrder.getParts().size());
		
		OrderPart orderPart = retrivedOrder.getParts().get(0);
		Assert.assertNotNull(orderPart);
		Assert.assertEquals(1, orderPart.getOrderLines().size());
		
		OrderLine line = orderPart.getOrderLines().get(0);
		Assert.assertNotNull(line);
		Assert.assertNotNull(line.getOffer());
		Assert.assertEquals(offer, line.getOffer());
	}
	
	@Test
	public void saveOneClickOrderWithOptions() {
		OLATResource randomOres1 = createResource();
		Offer offer = acService.createOffer(randomOres1, "TestSaveOneClickOrder 1");
		offer = acService.save(offer);

		dbInstance.commitAndCloseSession();
		
		//create a link offer to method
		List<AccessMethod> methods = acMethodManager.getAvailableMethodsByType(FreeAccessMethod.class);
		Assert.assertEquals(1, methods.size());
		AccessMethod method = methods.get(0);
		
		OfferAccess access1 = acMethodManager.createOfferAccess(offer, method);
		acMethodManager.save(access1);

		BillingAddress billingAddress = acService.createBillingAddress(null, null);

		dbInstance.commitAndCloseSession();
		
		//one clicks
		Order order = acOrderManager.saveOneClick(ident7, access1, OrderStatus.PREPAYMENT,
				billingAddress, "PO-234", "Useful comment");
		dbInstance.commitAndCloseSession();
		
		//load order
		Order retrivedOrder = acOrderManager.loadOrderByKey(order.getKey());
		Assert.assertEquals(order, retrivedOrder);
		Assert.assertEquals("PO-234", retrivedOrder.getPurchaseOrderNumber());
		Assert.assertEquals("Useful comment", retrivedOrder.getComment());
		Assert.assertEquals(billingAddress, retrivedOrder.getBillingAddress());
		Assert.assertEquals(OrderStatus.PREPAYMENT, retrivedOrder.getOrderStatus());
	}
	
	@Test
	public void countNativeOrderItems() {
		OLATResource randomOres1 = createResource();
		Offer offer = acService.createOffer(randomOres1, "Count order items 1");
		offer = acService.save(offer);

		dbInstance.commitAndCloseSession();
		
		//create a link offer to method
		List<AccessMethod> methods = acMethodManager.getAvailableMethodsByType(FreeAccessMethod.class);
		Assert.assertEquals(1, methods.size());
		AccessMethod method = methods.get(0);
		
		OfferAccess access1 = acMethodManager.createOfferAccess(offer, method);
		acMethodManager.save(access1);

		dbInstance.commitAndCloseSession();
		
		//one clicks
		Order order = acOrderManager.saveOneClick(ident7, access1, OrderStatus.PREPAYMENT,
				null, "PO-235", "Comment");
		dbInstance.commitAndCloseSession();
		
		int countOrders = acOrderManager.countNativeOrderItems(randomOres1, ident7, order.getKey(),
				DateUtils.addDays(new Date(), -2), DateUtils.addDays(new Date(), 2), OrderStatus.PREPAYMENT);
		Assert.assertEquals(1, countOrders);
		
		int countNewOrders = acOrderManager.countNativeOrderItems(randomOres1, ident7, null,
				null, null, OrderStatus.NEW);
		Assert.assertEquals(0, countNewOrders);
	}
	
	@Test
	public void saveOneClickOrderWithPrice() {
	//make extensiv test on one order
		//create some offers to buy
		OLATResource randomOres1 = createResource();
		Offer offer1 = acService.createOffer(randomOres1, "TestSaveOneClickOrder 1");
		Price price1 = new PriceImpl(new BigDecimal("20.00"), "CHF");
		offer1.setPrice(price1);
		offer1 = acService.save(offer1);

		dbInstance.commitAndCloseSession();
		
		//create a link offer to method
		List<AccessMethod> methods = acMethodManager.getAvailableMethodsByType(FreeAccessMethod.class);
		assertNotNull(methods);
		assertEquals(1, methods.size());
		AccessMethod method = methods.get(0);
		
		OfferAccess access1 = acMethodManager.createOfferAccess(offer1, method);
		acMethodManager.save(access1);

		dbInstance.commitAndCloseSession();
		
		//one clicks
		Order order = acOrderManager.saveOneClick(ident7, access1);
		
		dbInstance.commitAndCloseSession();
		
		//load order
		Order retrivedOrder = acOrderManager.loadOrderByKey(order.getKey());
		
		//check order
		assertNotNull(retrivedOrder);
		assertNotNull(retrivedOrder.getCreationDate());
		assertNotNull(retrivedOrder.getDelivery());
		assertNotNull(retrivedOrder.getOrderNr());
		assertNotNull(retrivedOrder.getParts());
		assertNotNull(retrivedOrder.getTotal());
		assertNotNull(retrivedOrder.getTotalOrderLines());

		assertEquals(ident7, retrivedOrder.getDelivery());
		assertEquals(1, retrivedOrder.getParts().size());
		
		assertEquals(price1.getAmount().setScale(2, RoundingMode.HALF_EVEN), retrivedOrder.getTotalOrderLines().getAmount().setScale(2, RoundingMode.HALF_EVEN));
		assertEquals(price1.getCurrencyCode(), retrivedOrder.getTotalOrderLines().getCurrencyCode());
		assertEquals(price1.getAmount().setScale(2, RoundingMode.HALF_EVEN), retrivedOrder.getTotal().getAmount().setScale(2, RoundingMode.HALF_EVEN));
		assertEquals(price1.getCurrencyCode(), retrivedOrder.getTotal().getCurrencyCode());	
		
		//check order part
		OrderPart orderPart = retrivedOrder.getParts().get(0);
		assertNotNull(orderPart);
		assertNotNull(orderPart.getTotal());
		assertNotNull(orderPart.getTotalOrderLines());
		assertEquals(1, orderPart.getOrderLines().size());
		assertEquals(price1.getAmount().setScale(2, RoundingMode.HALF_EVEN), orderPart.getTotalOrderLines().getAmount().setScale(2, RoundingMode.HALF_EVEN));
		assertEquals(price1.getCurrencyCode(), orderPart.getTotalOrderLines().getCurrencyCode());
		assertEquals(price1.getAmount().setScale(2, RoundingMode.HALF_EVEN), orderPart.getTotal().getAmount().setScale(2, RoundingMode.HALF_EVEN));
		assertEquals(price1.getCurrencyCode(), orderPart.getTotal().getCurrencyCode());	
		
		//check order line
		OrderLine line = orderPart.getOrderLines().get(0);
		Assert.assertNotNull(line);
		Assert.assertNotNull(line.getOffer());
		Assert.assertNotNull(line.getUnitPrice());
		Assert.assertNotNull(line.getTotal());
		Assert.assertEquals(offer1, line.getOffer());
		Assert.assertEquals(price1.getAmount().setScale(2, RoundingMode.HALF_EVEN), line.getUnitPrice().getAmount().setScale(2, RoundingMode.HALF_EVEN));
		Assert.assertEquals(price1.getCurrencyCode(), line.getUnitPrice().getCurrencyCode());
		Assert.assertEquals(price1.getAmount().setScale(2, RoundingMode.HALF_EVEN), line.getTotal().getAmount().setScale(2, RoundingMode.HALF_EVEN));
		Assert.assertEquals(price1.getCurrencyCode(), line.getTotal().getCurrencyCode());	
	}
	
	@Test
	public void findOrdersBy() {
		//create some offers to buy
		OLATResource randomOres1 = createResource();
		Offer offer1 = acService.createOffer(randomOres1, "TestLoadBy 1");
		offer1 = acService.save(offer1);
		
		OLATResource randomOres2 = createResource();
		Offer offer2 = acService.createOffer(randomOres2, "TestLoadBy 2");
		offer2 = acService.save(offer2);
		
		dbInstance.commitAndCloseSession();
		
		//create a link offer to method
		List<AccessMethod> methods = acMethodManager.getAvailableMethodsByType(TokenAccessMethod.class);
		assertNotNull(methods);
		assertEquals(1, methods.size());
		AccessMethod method = methods.get(0);
		
		OfferAccess access1 = acMethodManager.createOfferAccess(offer1, method);
		acMethodManager.save(access1);
		
		OfferAccess access2 = acMethodManager.createOfferAccess(offer2, method);
		acMethodManager.save(access2);

		dbInstance.commitAndCloseSession();
		
		//one clicks
		Order order1 = acOrderManager.saveOneClick(ident4, access1);
		Order order2 = acOrderManager.saveOneClick(ident5, access2);
		Order order3_1 = acOrderManager.saveOneClick(ident6, access1);
		Order order3_2 = acOrderManager.saveOneClick(ident6, access2);
		
		dbInstance.commitAndCloseSession();
		
		//load by delivery: ident 1
		List<Order> retrivedOrder1 = acOrderManager.findOrdersByDelivery(ident4);
		assertNotNull(retrivedOrder1);
		assertEquals(1, retrivedOrder1.size());
		assertEquals(order1, retrivedOrder1.get(0));
		
		//load by delivery: ident 2
		List<Order> retrievedOrder2 = acOrderManager.findOrdersByDelivery(ident5);
		assertNotNull(retrievedOrder2);
		assertEquals(1, retrievedOrder2.size());
		assertEquals(order2, retrievedOrder2.get(0));
		
		//load by delivery: ident 3
		List<Order> retrievedOrder3 = acOrderManager.findOrdersByDelivery(ident6);
		assertNotNull(retrievedOrder3);
		assertEquals(2, retrievedOrder3.size());
		assertTrue(order3_1.equals(retrievedOrder3.get(0)) || order3_1.equals(retrievedOrder3.get(1)));
		assertTrue(order3_2.equals(retrievedOrder3.get(0)) || order3_2.equals(retrievedOrder3.get(1)));

		dbInstance.commitAndCloseSession();
		
		//load by resource: ores 1
		List<Order> retrievedOrderOres1 = acOrderManager.findOrdersByResource(randomOres1);
		assertNotNull(retrievedOrderOres1);
		assertEquals(2, retrievedOrderOres1.size());
		assertTrue(order1.equals(retrievedOrderOres1.get(0)) || order1.equals(retrievedOrderOres1.get(1)));
		assertTrue(order3_1.equals(retrievedOrderOres1.get(0)) || order3_1.equals(retrievedOrderOres1.get(1)));
		
		//load by resource: ores 2
		List<Order> retrievedOrderOres2 = acOrderManager.findOrdersByResource(randomOres2);
		assertNotNull(retrievedOrderOres2);
		assertEquals(2, retrievedOrderOres2.size());
		assertTrue(order2.equals(retrievedOrderOres2.get(0)) || order2.equals(retrievedOrderOres2.get(1)));
		assertTrue(order3_2.equals(retrievedOrderOres2.get(0)) || order3_2.equals(retrievedOrderOres2.get(1)));
		
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void findOrdersByResourceByStatus() {
		//create some offers to buy
		OLATResource randomOres = createResource();
		Offer offer1 = acService.createOffer(randomOres, "TestLoadByStatus 1");
		offer1 = acService.save(offer1);

		dbInstance.commitAndCloseSession();
		
		//create a link offer to method
		List<AccessMethod> methods = acMethodManager.getAvailableMethodsByType(TokenAccessMethod.class);
		assertNotNull(methods);
		assertEquals(1, methods.size());
		AccessMethod method = methods.get(0);
		OfferAccess access1 = acMethodManager.createOfferAccess(offer1, method);
		acMethodManager.save(access1);
		
		dbInstance.commitAndCloseSession();
		
		//one clicks
		Order order1 = acOrderManager.saveOneClick(ident4, access1);
		Order order2 = acOrderManager.saveOneClick(ident6, access1);
		
		dbInstance.commitAndCloseSession();

		// Payed orders
		List<Order> retrievedOrders = acOrderManager.findOrdersByResource(randomOres, OrderStatus.PAYED);
		Assertions.assertThat(retrievedOrders)
			.hasSize(2)
			.containsExactlyInAnyOrder(order1, order2);
		
		// Pending orders
		List<Order> pendingOrders = acOrderManager.findOrdersByResource(randomOres, OrderStatus.PREPAYMENT);
		Assert.assertTrue(pendingOrders.isEmpty());
		
		// Payed orders
		List<Order> allOrders = acOrderManager.findOrdersByResource(randomOres);
		Assertions.assertThat(allOrders)
			.hasSize(2)
			.containsExactlyInAnyOrder(order1, order2);
	}
	
	@Test
	public void findOrdersByIdentityStatus() {
		//create some offers to buy
		OLATResource randomOres = createResource();
		Offer offer = acService.createOffer(randomOres, "TestLoadByIdentityStatus");
		offer = acService.save(offer);

		dbInstance.commitAndCloseSession();
		
		//create a link offer to method
		List<AccessMethod> methods = acMethodManager.getAvailableMethodsByType(TokenAccessMethod.class);
		assertNotNull(methods);
		assertEquals(1, methods.size());
		AccessMethod method = methods.get(0);
		OfferAccess access = acMethodManager.createOfferAccess(offer, method);
		acMethodManager.save(access);
		
		dbInstance.commitAndCloseSession();
		
		//one clicks
		Order order = acOrderManager.saveOneClick(ident4, access);
		
		dbInstance.commitAndCloseSession();

		// Payed orders
		List<Order> retrievedOrders = acOrderManager.findOrdersBy(ident4, randomOres, OrderStatus.PAYED);
		Assertions.assertThat(retrievedOrders)
			.hasSize(1)
			.containsExactlyInAnyOrder(order);
		
		// Pending orders
		List<Order> pendingOrders = acOrderManager.findOrdersBy(ident4, randomOres, OrderStatus.NEW);
		Assert.assertTrue(pendingOrders.isEmpty());
		
		// All orders
		List<Order> allOrders = acOrderManager.findOrdersBy(ident4, randomOres);
		Assertions.assertThat(allOrders)
			.hasSize(1)
			.containsExactlyInAnyOrder(order);
	}
	
	@Test
	public void deleteResource() {
		//create some offers to buy
		OLATResource randomOres1 = createResource();
		Offer offer1 = acService.createOffer(randomOres1, "TestDeleteResource 1");
		offer1 = acService.save(offer1);

		dbInstance.commitAndCloseSession();
		
		//create a link offer to method
		List<AccessMethod> methods = acMethodManager.getAvailableMethodsByType(TokenAccessMethod.class);
		assertNotNull(methods);
		assertEquals(1, methods.size());
		AccessMethod method = methods.get(0);
		
		OfferAccess access = acMethodManager.createOfferAccess(offer1, method);
		acMethodManager.save(access);

		dbInstance.commitAndCloseSession();
		
		//save an order
		Order order1 = acOrderManager.saveOneClick(ident8, access);
		dbInstance.commitAndCloseSession();

		//delete the resource
		randomOres1 = dbInstance.getCurrentEntityManager().find(OLATResourceImpl.class, randomOres1.getKey());
		dbInstance.deleteObject(randomOres1);
		
		dbInstance.commitAndCloseSession();
		
		//load order
		Order retrievedOrder1 = acOrderManager.loadOrderByKey(order1.getKey());
		assertNotNull(retrievedOrder1);
	}
	
	@Test
	public void findPendingOrders() {
		//create some offers to buy
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("pending-");
		OLATResource randomOres = createResource();
		Offer offer = acService.createOffer(randomOres, "Test pending resource 1");
		offer = acService.save(offer);
		dbInstance.commitAndCloseSession();
		
		//create a link offer to method
		List<AccessMethod> methods = acMethodManager.getAvailableMethodsByType(TokenAccessMethod.class);
		assertNotNull(methods);
		assertEquals(1, methods.size());
		AccessMethod method = methods.get(0);
		OfferAccess access = acMethodManager.createOfferAccess(offer, method);
		acMethodManager.save(access);
		dbInstance.commitAndCloseSession();
		
		//save an order
		Order order = acOrderManager.saveOneClick(id, access);
		((OrderImpl)order).setOrderStatus(OrderStatus.NEW);
		order = acOrderManager.save(order);
		dbInstance.commitAndCloseSession();
		
		List<Order> newOrders = acOrderManager.findPendingOrders(randomOres, id);
		Assert.assertTrue(newOrders.isEmpty());

		// add a transaction in new status
		OrderPart part = order.getParts().get(0);
		AccessTransaction accessTransaction = acTransactionManager.createTransaction(order, part, method);
		((AccessTransactionImpl)accessTransaction).setStatus(AccessTransactionStatus.NEW);
		acTransactionManager.save(accessTransaction);
		dbInstance.commitAndCloseSession();
		
		List<Order> reallyNewOrders = acOrderManager.findPendingOrders(randomOres, id);
		Assert.assertTrue(reallyNewOrders.isEmpty());
		
		// add a transaction pending
		order = acOrderManager.loadOrderByKey(order.getKey());
		((OrderImpl)order).setOrderStatus(OrderStatus.PREPAYMENT);
		order = acOrderManager.save(order);
		part = order.getParts().get(0);
		AccessTransaction pendingAccessTransaction = acTransactionManager.createTransaction(order, part, method);
		((AccessTransactionImpl)pendingAccessTransaction).setStatus(AccessTransactionStatus.PENDING);
		acTransactionManager.save(pendingAccessTransaction);
		dbInstance.commitAndCloseSession();
		
		List<Order> pendingOrders = acOrderManager.findPendingOrders(randomOres, id);
		Assert.assertFalse(pendingOrders.isEmpty());
		Assert.assertEquals(order, pendingOrders.get(0));
		
		// add a transaction success
		order = acOrderManager.loadOrderByKey(order.getKey());
		((OrderImpl)order).setOrderStatus(OrderStatus.PREPAYMENT);
		order = acOrderManager.save(order);
		part = order.getParts().get(0);
		AccessTransaction successAccessTransaction = acTransactionManager.createTransaction(order, part, method);
		((AccessTransactionImpl)successAccessTransaction).setStatus(AccessTransactionStatus.SUCCESS);
		acTransactionManager.save(successAccessTransaction);
		dbInstance.commitAndCloseSession();
		
		List<Order> successOrders = acOrderManager.findPendingOrders(randomOres, id);
		Assert.assertTrue(successOrders.isEmpty());
	}
	
	private OLATResource createResource() {
		//create a repository entry
		OLATResourceable resourceable = new TypedResourceable(UUID.randomUUID().toString().replace("-", ""));
		return resourceManager.createAndPersistOLATResourceInstance(resourceable);
	}
	
	@Test
	public void getBillingAddressKeyToOrderCount() {
		BillingAddress billingAddress1 = acService.createBillingAddress(null, null);
		BillingAddress billingAddress2 = acService.createBillingAddress(null, null);
		BillingAddress billingAddress3 = acService.createBillingAddress(null, null);
		BillingAddress billingAddress4 = acService.createBillingAddress(null, null);
		Order order11 = acOrderManager.createOrder(ident1);
		acService.addBillingAddress(order11, billingAddress1);
		Order order12 = acOrderManager.createOrder(ident1);
		acService.addBillingAddress(order12, billingAddress1);
		Order order21 = acOrderManager.createOrder(ident1);
		acService.addBillingAddress(order21, billingAddress2);
		Order order41 = acOrderManager.createOrder(ident1);
		acService.addBillingAddress(order41, billingAddress4);
		dbInstance.commitAndCloseSession();
		
		Map<Long, Long> billingAddressKeyToOrderCount = acOrderManager.getBillingAddressKeyToOrderCount(
				List.of(billingAddress1, billingAddress2, billingAddress3));
		Assert.assertEquals(Long.valueOf(2), billingAddressKeyToOrderCount.get(billingAddress1.getKey()));
		Assert.assertEquals(Long.valueOf(1), billingAddressKeyToOrderCount.get(billingAddress2.getKey()));
		Assert.assertNull(billingAddressKeyToOrderCount.get(billingAddress3.getKey()));
		Assert.assertNull(billingAddressKeyToOrderCount.get(billingAddress4.getKey()));
	}
	
	@Test
	public void hasOrder() {
		RepositoryEntry repositoryEntry = JunitTestHelper.createRandomRepositoryEntry(ident2);
		OLATResource randomOres = repositoryEntry.getOlatResource();
		Offer offer = acService.createOffer(randomOres, "TestSaveOrder");
		offer = acService.save(offer);
		dbInstance.commitAndCloseSession();
		
		assertFalse(acOrderManager.hasOrder(offer));
		
		AccessMethod method = acMethodManager.getAvailableMethodsByType(FreeAccessMethod.class).get(0);
		OfferAccess access = acMethodManager.createOfferAccess(offer, method);
		acService.accessResource(ident1, access, OrderStatus.PAYED, null, ident1);
		dbInstance.commitAndCloseSession();
		
		assertTrue(acOrderManager.hasOrder(offer));
	}
	
}
