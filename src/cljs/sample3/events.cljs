(ns sample3.events
  (:require
    [re-frame.core :as rf]
    [day8.re-frame.tracing :refer-macros [fn-traced]]
    [ajax.core :as ajax]
    [reitit.frontend.easy :as rfe]
    [reitit.frontend.controllers :as rfc]))






;;dispatchers



(rf/reg-event-db
  :init-db
  (fn-traced
    [db [_ key data]]
    (assoc db key data)))

(rf/reg-event-db
  :set-key
  (fn-traced
    [db [_ idx k val]]
    (prn ":set-key" idx k val)
    (assoc-in db [:equations idx k] val)))

(rf/dispatch [:set-key 1 :x 33])

(rf/reg-event-db
  :add-eq
  (fn-traced
    [db [_ k val]]
    (assoc db k val)))

(rf/reg-event-db
  :update-eq
  (fn-traced
    [db [_ eq k val]]
    (assoc-in db [eq k] val)))

(rf/dispatch [:add-eq :equation1 {:x 5 :y 2 :op "-" :total 3}])
(rf/dispatch [:update-eq :equation1 :y 4])

(rf/reg-event-db
  :common/navigate
  (fn [db [_ match]]
    (let [old-match (:common/route db)
          new-match (assoc match :controllers
                                 (rfc/apply-controllers (:controllers old-match) match))]
      (assoc db :common/route new-match))))

(rf/reg-fx
  :common/navigate-fx!
  (fn [[k & [params query]]]
    (rfe/push-state k params query)))

(rf/reg-event-fx
  :common/navigate!
  (fn [_ [_ url-key params query]]
    {:common/navigate-fx! [url-key params query]}))

(rf/reg-event-db
  :set-docs
  (fn [db [_ docs]]
    (assoc db :docs docs)))

(rf/reg-event-fx
  :fetch-docs
  (fn [_ _]
    {:http-xhrio {:method          :get
                  :uri             "/docs"
                  :response-format (ajax/raw-response-format)
                  :on-success       [:set-docs]}}))

(rf/reg-event-db
  :common/set-error
  (fn [db [_ error]]
    (assoc db :common/error error)))

(rf/reg-event-fx
  :page/init-home
  (fn [_ _]
    {:dispatch [:fetch-docs]}))









;;subscriptions


(rf/reg-sub
  :equations
  (fn [db _]
    (-> db :equations)))

(comment
  @(rf/subscribe [:equations]))



(rf/reg-sub
  :one-equation
  (fn [db [_ idx]]
    (get (-> db :equations) idx)))

@(rf/subscribe [:one-equation 0])

(rf/reg-sub
  :get-eq
  (fn [db [_ eq]]
    (eq db)))  ;; eq will be a key, and the db is just a big map
               ;; so I can use the standard (:key {map}) notation
               ;; to get the value I want out  (-> db eq) would also work

@(rf/subscribe [:get-eq :equation1])



(rf/reg-sub
  :common/route
  (fn [db _]
    (-> db :common/route)))

(rf/reg-sub
  :common/page-id
  :<- [:common/route]
  (fn [route _]
    (-> route :data :name)))

(rf/reg-sub
  :common/page
  :<- [:common/route]
  (fn [route _]
    (-> route :data :view)))

(rf/reg-sub
  :docs
  (fn [db _]
    (:docs db)))

(rf/reg-sub
  :common/error
  (fn [db _]
    (:common/error db)))
