package com.heena.user.models;

import androidx.annotation.XmlRes;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "API3G")
public class MyXMLDataModel {
    String CompanyToken;
    String Request;

    @XmlRootElement(name = "Transaction")
    class Transaction{
        Double PaymentAmount;
        String PaymentCurrency;
        String CompanyRef;
        String RedirectURL;
        String BackURL;
        String CompanyRefUnique;
        String PTL;

        public Double getPaymentAmount() {
            return PaymentAmount;
        }

        public void setPaymentAmount(Double paymentAmount) {
            PaymentAmount = paymentAmount;
        }

        public String getPaymentCurrency() {
            return PaymentCurrency;
        }

        public void setPaymentCurrency(String paymentCurrency) {
            PaymentCurrency = paymentCurrency;
        }

        public String getCompanyRef() {
            return CompanyRef;
        }

        public void setCompanyRef(String companyRef) {
            CompanyRef = companyRef;
        }

        public String getRedirectURL() {
            return RedirectURL;
        }

        public void setRedirectURL(String redirectURL) {
            RedirectURL = redirectURL;
        }

        public String getBackURL() {
            return BackURL;
        }

        public void setBackURL(String backURL) {
            BackURL = backURL;
        }

        public String getCompanyRefUnique() {
            return CompanyRefUnique;
        }

        public void setCompanyRefUnique(String companyRefUnique) {
            CompanyRefUnique = companyRefUnique;
        }

        public String getPTL() {
            return PTL;
        }

        public void setPTL(String PTL) {
            this.PTL = PTL;
        }
    }

    @XmlRootElement(name = "Services")
    class Services{
        @XmlRootElement(name = "Service")
        class Service{
            String ServiceType;
            String ServiceDescription;
            String ServiceDate;

            public String getServiceType() {
                return ServiceType;
            }

            public void setServiceType(String serviceType) {
                ServiceType = serviceType;
            }

            public String getServiceDescription() {
                return ServiceDescription;
            }

            public void setServiceDescription(String serviceDescription) {
                ServiceDescription = serviceDescription;
            }

            public String getServiceDate() {
                return ServiceDate;
            }

            public void setServiceDate(String serviceDate) {
                ServiceDate = serviceDate;
            }
        }
    }

    public String getCompanyToken() {
        return CompanyToken;
    }

    public void setCompanyToken(String companyToken) {
        CompanyToken = companyToken;
    }

    public String getRequest() {
        return Request;
    }

    public void setRequest(String request) {
        Request = request;
    }
}
