import { useState, useEffect, useRef, useCallback } from 'react'
import {
  Globe, Building2, Phone, Mail, MapPin, User, ChevronRight, ChevronLeft,
  CheckCircle, Search, AlertCircle, Upload, X, ShoppingBag, CreditCard,
  Package, Info, Loader2, Shield, TrendingUp, Settings, Users,
} from 'lucide-react'
import { tenantFeesApi } from '../../api/tenantFeesApi'
import { tenantApi } from '../../api/tenantApi'
import { userApi } from '../../api/userApi'

// ─── Translations ─────────────────────────────────────────────────────────────
const T = {
  en: {
    brandName: 'MyOnline',
    brandTagline: 'One Platform. Multiple Stores.',
    heroTitle: 'Launch Your Online Store Today',
    heroDesc: 'Register your business and get your own e-commerce storefront. Choose a plan, pick your domain, and start selling in minutes.',
    featureMultiTenant: 'Multi-Tenant',
    featureMultiTenantDesc: 'Your own isolated store',
    featureSecure: 'Secure',
    featureSecureDesc: 'Enterprise-grade protection',
    featureScalable: 'Scalable',
    featureScalableDesc: 'Grows with your business',
    featureCustom: 'Customizable',
    featureCustomDesc: 'Your brand, your style',
    steps: ['Business Info', 'Select Package', 'Choose Domain', 'Payment'],
    // Step 1
    s1Title: 'Business Information',
    s1Subtitle: 'Tell us about your business',
    businessName: 'Business Name',
    businessNamePh: 'Enter your business name',
    address1: 'Mailing Address',
    address1Ph: 'Street address, area, district',
    address2: 'Address Line 2 (Optional)',
    address2Ph: 'Apartment, floor, additional info',
    contactPerson: 'Contact Person',
    contactPersonPh: 'Full name of primary contact',
    phone1: 'Phone Number',
    phone1Ph: '01XXXXXXXXX',
    phone2: 'Secondary Phone (Optional)',
    phone2Ph: '01XXXXXXXXX',
    email: 'Email Address',
    emailPh: 'owner@yourbusiness.com',
    // Step 2
    s2Title: 'Select Your Package',
    s2Subtitle: 'How many products do you plan to sell?',
    productCount: 'Number of Products',
    productCountPh: 'e.g., 50',
    productCountHint: 'We\'ll automatically recommend the best package for your needs',
    loadingPkg: 'Loading packages...',
    noPackage: 'No package available for this product count. Please try a different number.',
    recommendedPkg: 'Recommended Package',
    registrationFee: 'Registration Fee',
    monthlyFee: 'Monthly Fee',
    productRange: 'Product Range',
    products: 'products',
    // Step 3
    s3Title: 'Choose Your Domain',
    s3Subtitle: 'Your store will be accessible at this address',
    domainPrefix: 'Domain Prefix',
    domainPrefixPh: 'yourstore',
    domainPrefixInfo: 'Your store URL will be',
    domainRules: 'Lowercase letters, numbers, and hyphens only. No spaces.',
    domainChecking: 'Checking availability...',
    domainAvailable: 'Great! This domain is available.',
    domainTaken: 'This domain is already taken. Please try another.',
    // Step 4
    s4Title: 'Payment Information',
    s4Subtitle: 'Submit your registration fee payment (optional)',
    paymentNote: 'Payment details are optional. You can also pay after your registration is reviewed.',
    paymentChannel: 'Payment Channel',
    paymentChannelPh: 'e.g., bKash, Nagad, Bank Transfer',
    paymentRef: 'Transaction Reference',
    paymentRefPh: 'Transaction ID or reference number',
    paymentReceipt: 'Payment Receipt',
    paymentReceiptHint: 'Upload a screenshot or photo of your payment slip',
    uploadReceipt: 'Click to upload receipt image',
    receiptFormats: 'PNG, JPG, JPEG — max 5 MB',
    removeFile: 'Remove',
    // Buttons
    next: 'Next',
    back: 'Back',
    submit: 'Submit Registration',
    submitting: 'Submitting...',
    // Success
    successTitle: 'Registration Submitted!',
    successMsg: 'Your registration request has been received. Our team will review your application and contact you within 2–3 business days.',
    regId: 'Registration ID',
    yourDomain: 'Your Domain',
    yourPkg: 'Package',
    registerAnother: 'Register Another Business',
    goAdmin: 'Go to Admin Portal',
    // Errors
    errRequired: 'This field is required',
    errEmail: 'Please enter a valid email address',
    errEmailTaken: 'This email is already registered. Please use a different address.',
    errMobileTaken: 'This phone number is already registered. Please use a different number.',
    emailChecking: 'Checking email availability...',
    emailAvailable: 'Email is available.',
    mobileChecking: 'Checking phone number...',
    mobileAvailable: 'Phone number is available.',
    errDomainFormat: 'Only lowercase letters, numbers, and hyphens are allowed',
    errDomainUnavailable: 'Please choose an available domain',
    errNoPackage: 'Please enter a valid product count to select a package',
    errMinCount: 'Product count must be at least 1',
  },
  bn: {
    brandName: 'মাইঅনলাইন',
    brandTagline: 'একটি প্ল্যাটফর্ম। অনেক স্টোর।',
    heroTitle: 'আজই আপনার অনলাইন স্টোর চালু করুন',
    heroDesc: 'আপনার ব্যবসা নিবন্ধন করুন এবং আপনার নিজস্ব ই-কমার্স স্টোর পান। একটি প্ল্যান বেছে নিন, ডোমেইন বাছুন এবং কয়েক মিনিটের মধ্যে বিক্রি শুরু করুন।',
    featureMultiTenant: 'মাল্টি-টেন্যান্ট',
    featureMultiTenantDesc: 'আপনার নিজস্ব আলাদা স্টোর',
    featureSecure: 'নিরাপদ',
    featureSecureDesc: 'এন্টারপ্রাইজ মানের সুরক্ষা',
    featureScalable: 'স্কেলেবল',
    featureScalableDesc: 'আপনার ব্যবসার সাথে বাড়ে',
    featureCustom: 'কাস্টমাইজযোগ্য',
    featureCustomDesc: 'আপনার ব্র্যান্ড, আপনার স্টাইল',
    steps: ['ব্যবসার তথ্য', 'প্যাকেজ নির্বাচন', 'ডোমেইন বাছুন', 'পেমেন্ট'],
    // Step 1
    s1Title: 'ব্যবসার তথ্য',
    s1Subtitle: 'আপনার ব্যবসা সম্পর্কে জানান',
    businessName: 'ব্যবসার নাম',
    businessNamePh: 'আপনার ব্যবসার নাম লিখুন',
    address1: 'ডাক ঠিকানা',
    address1Ph: 'রাস্তা, এলাকা, জেলা',
    address2: 'ঠিকানা লাইন ২ (ঐচ্ছিক)',
    address2Ph: 'অ্যাপার্টমেন্ট, তলা, অতিরিক্ত তথ্য',
    contactPerson: 'যোগাযোগের ব্যক্তি',
    contactPersonPh: 'প্রধান যোগাযোগ ব্যক্তির পুরো নাম',
    phone1: 'ফোন নম্বর',
    phone1Ph: '০১XXXXXXXXX',
    phone2: 'দ্বিতীয় ফোন (ঐচ্ছিক)',
    phone2Ph: '০১XXXXXXXXX',
    email: 'ইমেইল ঠিকানা',
    emailPh: 'আপনার@ব্যবসা.com',
    // Step 2
    s2Title: 'আপনার প্যাকেজ নির্বাচন করুন',
    s2Subtitle: 'আপনি কতটি পণ্য বিক্রি করতে চান?',
    productCount: 'পণ্যের সংখ্যা',
    productCountPh: 'যেমন: ৫০',
    productCountHint: 'আমরা স্বয়ংক্রিয়ভাবে আপনার জন্য সেরা প্যাকেজ সুপারিশ করব',
    loadingPkg: 'প্যাকেজ লোড হচ্ছে...',
    noPackage: 'এই পণ্য সংখ্যার জন্য কোনো প্যাকেজ পাওয়া যায়নি। অন্য সংখ্যা চেষ্টা করুন।',
    recommendedPkg: 'প্রস্তাবিত প্যাকেজ',
    registrationFee: 'নিবন্ধন ফি',
    monthlyFee: 'মাসিক ফি',
    productRange: 'পণ্যের সীমা',
    products: 'পণ্য',
    // Step 3
    s3Title: 'আপনার ডোমেইন বেছে নিন',
    s3Subtitle: 'আপনার স্টোর এই ঠিকানায় পাওয়া যাবে',
    domainPrefix: 'ডোমেইন প্রিফিক্স',
    domainPrefixPh: 'আপনারস্টোর',
    domainPrefixInfo: 'আপনার স্টোরের URL হবে',
    domainRules: 'শুধুমাত্র ছোট হাতের অক্ষর, সংখ্যা এবং হাইফেন। কোনো স্পেস নয়।',
    domainChecking: 'প্রাপ্যতা যাচাই হচ্ছে...',
    domainAvailable: 'দারুণ! এই ডোমেইনটি উপলব্ধ।',
    domainTaken: 'এই ডোমেইনটি ইতিমধ্যে নেওয়া হয়েছে। অন্যটি চেষ্টা করুন।',
    // Step 4
    s4Title: 'পেমেন্ট তথ্য',
    s4Subtitle: 'নিবন্ধন ফি পেমেন্টের বিবরণ দিন (ঐচ্ছিক)',
    paymentNote: 'পেমেন্টের তথ্য ঐচ্ছিক। আপনার আবেদন পর্যালোচনার পরেও পেমেন্ট করতে পারবেন।',
    paymentChannel: 'পেমেন্ট চ্যানেল',
    paymentChannelPh: 'যেমন: বিকাশ, নগদ, ব্যাংক ট্রান্সফার',
    paymentRef: 'লেনদেন রেফারেন্স',
    paymentRefPh: 'ট্রানজেকশন আইডি বা রেফারেন্স নম্বর',
    paymentReceipt: 'পেমেন্ট রসিদ',
    paymentReceiptHint: 'পেমেন্ট স্লিপের স্ক্রিনশট বা ছবি আপলোড করুন',
    uploadReceipt: 'রসিদের ছবি আপলোড করতে ক্লিক করুন',
    receiptFormats: 'PNG, JPG, JPEG — সর্বোচ্চ ৫ MB',
    removeFile: 'সরান',
    // Buttons
    next: 'পরবর্তী',
    back: 'পূর্ববর্তী',
    submit: 'নিবন্ধন জমা দিন',
    submitting: 'জমা দেওয়া হচ্ছে...',
    // Success
    successTitle: 'নিবন্ধন সম্পন্ন হয়েছে!',
    successMsg: 'আপনার নিবন্ধন আবেদন পাওয়া গেছে। আমাদের দল আপনার আবেদন পর্যালোচনা করবে এবং ২–৩ কার্যদিবসের মধ্যে যোগাযোগ করবে।',
    regId: 'নিবন্ধন আইডি',
    yourDomain: 'আপনার ডোমেইন',
    yourPkg: 'প্যাকেজ',
    registerAnother: 'আরেকটি ব্যবসা নিবন্ধন করুন',
    goAdmin: 'অ্যাডমিন পোর্টালে যান',
    // Errors
    errRequired: 'এই ক্ষেত্রটি আবশ্যক',
    errEmail: 'একটি বৈধ ইমেইল ঠিকানা লিখুন',
    errEmailTaken: 'এই ইমেইল ইতিমধ্যে নিবন্ধিত। অন্য একটি ঠিকানা ব্যবহার করুন।',
    errMobileTaken: 'এই ফোন নম্বরটি ইতিমধ্যে নিবন্ধিত। অন্য নম্বর ব্যবহার করুন।',
    emailChecking: 'ইমেইল যাচাই হচ্ছে...',
    emailAvailable: 'ইমেইল উপলব্ধ।',
    mobileChecking: 'ফোন নম্বর যাচাই হচ্ছে...',
    mobileAvailable: 'ফোন নম্বর উপলব্ধ।',
    errDomainFormat: 'শুধুমাত্র ছোট হাতের অক্ষর, সংখ্যা এবং হাইফেন ব্যবহার করুন',
    errDomainUnavailable: 'একটি উপলব্ধ ডোমেইন বেছে নিন',
    errNoPackage: 'প্যাকেজ নির্বাচন করতে বৈধ পণ্য সংখ্যা লিখুন',
    errMinCount: 'পণ্যের সংখ্যা কমপক্ষে ১ হতে হবে',
  },
}

const features = [
  { icon: Users, key: 'featureMultiTenant', descKey: 'featureMultiTenantDesc', color: 'text-blue-500', bg: 'bg-blue-50' },
  { icon: Shield, key: 'featureSecure', descKey: 'featureSecureDesc', color: 'text-green-500', bg: 'bg-green-50' },
  { icon: TrendingUp, key: 'featureScalable', descKey: 'featureScalableDesc', color: 'text-purple-500', bg: 'bg-purple-50' },
  { icon: Settings, key: 'featureCustom', descKey: 'featureCustomDesc', color: 'text-orange-500', bg: 'bg-orange-50' },
]

const DOMAIN_REGEX = /^[a-z0-9-]+$/

function formatCurrency(amount) {
  if (amount == null) return '—'
  return Number(amount).toLocaleString('en-BD')
}

// ─── Main Component ────────────────────────────────────────────────────────────
export default function RegisterPage() {
  const [lang, setLang] = useState('en')
  const t = T[lang]

  const [step, setStep] = useState(1)
  const [packages, setPackages] = useState([])
  const [pkgLoading, setPkgLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [submittedReg, setSubmittedReg] = useState(null)

  const [form, setForm] = useState({
    tenantBusinessName: '',
    mailingAddress1: '',
    mailingAddress2: '',
    contactPerson: '',
    contactNumber1: '',
    contactNumber2: '',
    emailAddress: '',
    maxInventoryItems: '',
    packageCode: '',
    domainPrefix: '',
    registrationFeePmtChannel: '',
    registrationFeePmtRef: '',
    registrationFeePmtReceiptBase64: '',
  })

  const [errors, setErrors] = useState({})
  const [selectedPackage, setSelectedPackage] = useState(null)
  const [domainStatus, setDomainStatus] = useState(null) // { available, message } | null
  const [domainChecking, setDomainChecking] = useState(false)
  const [emailStatus, setEmailStatus] = useState(null)   // { available: bool } | null
  const [emailChecking, setEmailChecking] = useState(false)
  const [mobileStatus, setMobileStatus] = useState(null) // { available: bool } | null
  const [mobileChecking, setMobileChecking] = useState(false)
  const [receiptFileName, setReceiptFileName] = useState('')
  const domainDebounceRef = useRef(null)
  const emailDebounceRef = useRef(null)
  const mobileDebounceRef = useRef(null)
  const fileInputRef = useRef(null)

  // Fetch packages on mount
  useEffect(() => {
    tenantFeesApi.getAll()
      .then((res) => setPackages(res.data.data || []))
      .catch(() => setPackages([]))
      .finally(() => setPkgLoading(false))
  }, [])

  // Auto-select package when product count changes
  useEffect(() => {
    const count = parseInt(form.maxInventoryItems, 10)
    if (!count || count < 1) {
      setSelectedPackage(null)
      return
    }
    const match = packages.find(
      (p) => count >= p.productCountFrom && count <= p.productCountTo
    )
    setSelectedPackage(match || null)
    setForm((prev) => ({ ...prev, packageCode: match?.packageCode || '' }))
  }, [form.maxInventoryItems, packages])

  // Debounced domain availability check
  const checkDomain = useCallback((prefix) => {
    if (!prefix || !DOMAIN_REGEX.test(prefix)) {
      setDomainStatus(null)
      return
    }
    if (domainDebounceRef.current) clearTimeout(domainDebounceRef.current)
    domainDebounceRef.current = setTimeout(async () => {
      setDomainChecking(true)
      try {
        const res = await tenantApi.checkDomain(prefix)
        const data = res.data.data
        setDomainStatus({ available: data.available, message: data.message })
      } catch {
        setDomainStatus(null)
      } finally {
        setDomainChecking(false)
      }
    }, 600)
  }, [])

  // Debounced email uniqueness check against auth_service
  const checkEmailUniqueness = useCallback((email) => {
    if (!email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      setEmailStatus(null)
      return
    }
    if (emailDebounceRef.current) clearTimeout(emailDebounceRef.current)
    emailDebounceRef.current = setTimeout(async () => {
      setEmailChecking(true)
      try {
        const res = await userApi.checkCount({ email })
        const count = res.data.data.count
        setEmailStatus({ available: count === 0 })
      } catch {
        setEmailStatus(null)
      } finally {
        setEmailChecking(false)
      }
    }, 600)
  }, [])

  // Debounced mobile uniqueness check against auth_service
  const checkMobileUniqueness = useCallback((mobile) => {
    if (!mobile || mobile.trim().length < 3) {
      setMobileStatus(null)
      return
    }
    if (mobileDebounceRef.current) clearTimeout(mobileDebounceRef.current)
    mobileDebounceRef.current = setTimeout(async () => {
      setMobileChecking(true)
      try {
        const res = await userApi.checkCount({ mobile })
        const count = res.data.data.count
        setMobileStatus({ available: count === 0 })
      } catch {
        setMobileStatus(null)
      } finally {
        setMobileChecking(false)
      }
    }, 600)
  }, [])

  const handleChange = (field) => (e) => {
    const value = e.target.value
    setForm((prev) => ({ ...prev, [field]: value }))
    setErrors((prev) => ({ ...prev, [field]: '' }))
    if (field === 'domainPrefix') {
      setDomainStatus(null)
      checkDomain(value)
    }
    if (field === 'emailAddress') {
      setEmailStatus(null)
      checkEmailUniqueness(value)
    }
    if (field === 'contactNumber1') {
      setMobileStatus(null)
      checkMobileUniqueness(value)
    }
  }

  // File upload → base64
  const handleFileChange = (e) => {
    const file = e.target.files[0]
    if (!file) return
    if (file.size > 5 * 1024 * 1024) {
      setErrors((prev) => ({ ...prev, receipt: 'File size must be under 5 MB' }))
      return
    }
    setReceiptFileName(file.name)
    const reader = new FileReader()
    reader.onload = () => {
      const base64 = reader.result.split(',')[1]
      setForm((prev) => ({ ...prev, registrationFeePmtReceiptBase64: base64 }))
    }
    reader.readAsDataURL(file)
  }

  const removeFile = () => {
    setReceiptFileName('')
    setForm((prev) => ({ ...prev, registrationFeePmtReceiptBase64: '' }))
    if (fileInputRef.current) fileInputRef.current.value = ''
  }

  // ── Validation ────────────────────────────────────────────────────────────────
  const validateStep = (s) => {
    const e = {}
    if (s === 1) {
      if (!form.tenantBusinessName.trim()) e.tenantBusinessName = t.errRequired
      if (!form.mailingAddress1.trim()) e.mailingAddress1 = t.errRequired
      if (!form.contactPerson.trim()) e.contactPerson = t.errRequired
      if (!form.contactNumber1.trim()) e.contactNumber1 = t.errRequired
      else if (mobileChecking) e.contactNumber1 = t.mobileChecking
      else if (mobileStatus?.available === false) e.contactNumber1 = t.errMobileTaken
      if (!form.emailAddress.trim()) e.emailAddress = t.errRequired
      else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.emailAddress)) e.emailAddress = t.errEmail
      else if (emailChecking) e.emailAddress = t.emailChecking
      else if (emailStatus?.available === false) e.emailAddress = t.errEmailTaken
    }
    if (s === 2) {
      const count = parseInt(form.maxInventoryItems, 10)
      if (!form.maxInventoryItems) e.maxInventoryItems = t.errRequired
      else if (count < 1) e.maxInventoryItems = t.errMinCount
      if (!selectedPackage) e.packageCode = t.errNoPackage
    }
    if (s === 3) {
      if (!form.domainPrefix.trim()) e.domainPrefix = t.errRequired
      else if (!DOMAIN_REGEX.test(form.domainPrefix)) e.domainPrefix = t.errDomainFormat
      else if (domainStatus && !domainStatus.available) e.domainPrefix = t.errDomainUnavailable
      else if (!domainStatus) e.domainPrefix = t.errDomainUnavailable
    }
    setErrors(e)
    return Object.keys(e).length === 0
  }

  const handleNext = () => {
    if (validateStep(step)) setStep((s) => s + 1)
  }

  const handleBack = () => setStep((s) => s - 1)

  const handleSubmit = async () => {
    if (!validateStep(4)) return
    setSubmitting(true)
    try {
      const payload = {
        tenantBusinessName: form.tenantBusinessName,
        mailingAddress1: form.mailingAddress1,
        mailingAddress2: form.mailingAddress2 || undefined,
        contactPerson: form.contactPerson,
        contactNumber1: form.contactNumber1,
        contactNumber2: form.contactNumber2 || undefined,
        emailAddress: form.emailAddress,
        maxInventoryItems: parseInt(form.maxInventoryItems, 10),
        packageCode: form.packageCode,
        domainPrefix: form.domainPrefix,
        registrationFeePmtChannel: form.registrationFeePmtChannel || undefined,
        registrationFeePmtRef: form.registrationFeePmtRef || undefined,
        registrationFeePmtReceiptBase64: form.registrationFeePmtReceiptBase64 || undefined,
      }
      const res = await tenantApi.submitRegistration(payload)
      setSubmittedReg(res.data.data)
    } catch (err) {
      const msg = err?.response?.data?.message || 'Submission failed. Please try again.'
      setErrors((prev) => ({ ...prev, _global: msg }))
    } finally {
      setSubmitting(false)
    }
  }

  const resetForm = () => {
    setStep(1)
    setForm({
      tenantBusinessName: '', mailingAddress1: '', mailingAddress2: '',
      contactPerson: '', contactNumber1: '', contactNumber2: '',
      emailAddress: '', maxInventoryItems: '', packageCode: '',
      domainPrefix: '', registrationFeePmtChannel: '',
      registrationFeePmtRef: '', registrationFeePmtReceiptBase64: '',
    })
    setErrors({})
    setSelectedPackage(null)
    setDomainStatus(null)
    setEmailStatus(null)
    setMobileStatus(null)
    setReceiptFileName('')
    setSubmittedReg(null)
  }

  // ── Render ────────────────────────────────────────────────────────────────────
  return (
    <div className="min-h-screen bg-slate-100 flex flex-col">
      {/* Top nav bar */}
      <header className="bg-white border-b border-gray-200 sticky top-0 z-50 shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-14 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <img src="/logo.jpeg" alt="MyOnline" className="w-8 h-8 object-contain rounded-lg" />
            <span className="font-bold text-gray-900 text-lg">{t.brandName}</span>
            <span className="hidden sm:block text-gray-400 text-xs ml-2">{t.brandTagline}</span>
          </div>
          {/* Language toggle */}
          <button
            onClick={() => setLang(lang === 'en' ? 'bn' : 'en')}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg border border-gray-200 hover:bg-gray-50 transition-colors text-sm font-medium text-gray-700"
          >
            <Globe className="w-4 h-4 text-primary-600" />
            {lang === 'en' ? 'বাংলা' : 'English'}
          </button>
        </div>
      </header>

      {/* Success screen */}
      {submittedReg ? (
        <SuccessScreen t={t} reg={submittedReg} pkg={selectedPackage} onReset={resetForm} lang={lang} />
      ) : (
        <main className="flex-1 max-w-7xl mx-auto w-full px-4 sm:px-6 lg:px-8 py-8 lg:py-12">
          <div className="lg:grid lg:grid-cols-5 lg:gap-10">

            {/* ── Left Panel (hero) ── */}
            <aside className="hidden lg:flex lg:col-span-2 flex-col">
              {/* Hero text */}
              <div className="bg-gradient-to-br from-primary-600 to-primary-800 rounded-2xl p-8 text-white mb-6">
                <div className="w-16 h-16 bg-white/20 rounded-2xl flex items-center justify-center mb-5">
                  <ShoppingBag className="w-8 h-8 text-white" />
                </div>
                <h2 className="text-2xl font-bold mb-3 leading-tight">{t.heroTitle}</h2>
                <p className="text-primary-100 text-sm leading-relaxed">{t.heroDesc}</p>
              </div>
              {/* Feature grid */}
              <div className="grid grid-cols-2 gap-3">
                {features.map(({ icon: Icon, key, descKey, color, bg }) => (
                  <div key={key} className="bg-white rounded-xl p-4 border border-gray-100 shadow-sm">
                    <div className={`w-9 h-9 rounded-lg ${bg} flex items-center justify-center mb-2`}>
                      <Icon className={`w-5 h-5 ${color}`} />
                    </div>
                    <p className="font-semibold text-gray-800 text-sm">{t[key]}</p>
                    <p className="text-gray-500 text-xs mt-0.5">{t[descKey]}</p>
                  </div>
                ))}
              </div>
            </aside>

            {/* ── Right Panel (form) ── */}
            <section className="lg:col-span-3">
              {/* Step indicator */}
              <StepIndicator step={step} labels={t.steps} />

              {/* Form card */}
              <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 sm:p-8 mt-6">
                {step === 1 && (
                  <Step1
                    t={t} form={form} errors={errors} onChange={handleChange}
                    emailStatus={emailStatus} emailChecking={emailChecking}
                    mobileStatus={mobileStatus} mobileChecking={mobileChecking}
                  />
                )}
                {step === 2 && (
                  <Step2
                    t={t} form={form} errors={errors} onChange={handleChange}
                    packages={packages} pkgLoading={pkgLoading} selectedPackage={selectedPackage}
                  />
                )}
                {step === 3 && (
                  <Step3
                    t={t} form={form} errors={errors} onChange={handleChange}
                    domainStatus={domainStatus} domainChecking={domainChecking}
                  />
                )}
                {step === 4 && (
                  <Step4
                    t={t} form={form} errors={errors} onChange={handleChange}
                    receiptFileName={receiptFileName} onFileChange={handleFileChange}
                    onRemoveFile={removeFile} fileInputRef={fileInputRef}
                    selectedPackage={selectedPackage}
                  />
                )}

                {/* Global error */}
                {errors._global && (
                  <div className="mt-4 p-3 bg-red-50 border border-red-200 rounded-lg flex items-start gap-2">
                    <AlertCircle className="w-4 h-4 text-red-500 mt-0.5 shrink-0" />
                    <p className="text-sm text-red-700">{errors._global}</p>
                  </div>
                )}

                {/* Navigation */}
                <div className="flex items-center justify-between mt-8 pt-5 border-t border-gray-100">
                  <button
                    onClick={handleBack}
                    disabled={step === 1}
                    className="flex items-center gap-1.5 px-4 py-2 rounded-lg border border-gray-300 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
                  >
                    <ChevronLeft className="w-4 h-4" />
                    {t.back}
                  </button>

                  {step < 4 ? (
                    <button
                      onClick={handleNext}
                      className="flex items-center gap-1.5 px-5 py-2 rounded-lg bg-primary-600 text-white text-sm font-medium hover:bg-primary-700 transition-colors"
                    >
                      {t.next}
                      <ChevronRight className="w-4 h-4" />
                    </button>
                  ) : (
                    <button
                      onClick={handleSubmit}
                      disabled={submitting}
                      className="flex items-center gap-1.5 px-5 py-2 rounded-lg bg-primary-600 text-white text-sm font-medium hover:bg-primary-700 disabled:opacity-60 disabled:cursor-not-allowed transition-colors"
                    >
                      {submitting ? (
                        <>
                          <Loader2 className="w-4 h-4 animate-spin" />
                          {t.submitting}
                        </>
                      ) : (
                        <>
                          <CheckCircle className="w-4 h-4" />
                          {t.submit}
                        </>
                      )}
                    </button>
                  )}
                </div>
              </div>
            </section>
          </div>
        </main>
      )}

      {/* Footer */}
      <footer className="text-center py-4 text-xs text-gray-400 border-t border-gray-200 bg-white mt-auto">
        © 2025 MyOnline. All rights reserved.
      </footer>
    </div>
  )
}

// ─── Step Indicator ────────────────────────────────────────────────────────────
function StepIndicator({ step, labels }) {
  return (
    <div className="flex items-center gap-0">
      {labels.map((label, i) => {
        const num = i + 1
        const done = num < step
        const active = num === step
        return (
          <div key={num} className="flex items-center flex-1 last:flex-none">
            <div className="flex flex-col items-center">
              <div
                className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-semibold transition-colors ${
                  done
                    ? 'bg-primary-600 text-white'
                    : active
                    ? 'bg-primary-600 text-white ring-4 ring-primary-100'
                    : 'bg-gray-200 text-gray-500'
                }`}
              >
                {done ? <CheckCircle className="w-4 h-4" /> : num}
              </div>
              <span
                className={`mt-1 text-xs font-medium hidden sm:block ${
                  active ? 'text-primary-700' : done ? 'text-primary-500' : 'text-gray-400'
                }`}
              >
                {label}
              </span>
            </div>
            {i < labels.length - 1 && (
              <div
                className={`flex-1 h-0.5 mx-1 mt-[-12px] sm:mt-[-16px] transition-colors ${
                  done ? 'bg-primary-600' : 'bg-gray-200'
                }`}
              />
            )}
          </div>
        )
      })}
    </div>
  )
}

// ─── Step 1: Business Information ─────────────────────────────────────────────
function Step1({ t, form, errors, onChange, emailStatus, emailChecking, mobileStatus, mobileChecking }) {
  return (
    <div>
      <h2 className="text-xl font-bold text-gray-900">{t.s1Title}</h2>
      <p className="text-sm text-gray-500 mt-1 mb-6">{t.s1Subtitle}</p>
      <div className="space-y-4">
        <Field label={t.businessName} error={errors.tenantBusinessName} icon={<Building2 className="w-4 h-4" />}>
          <input
            type="text"
            value={form.tenantBusinessName}
            onChange={onChange('tenantBusinessName')}
            placeholder={t.businessNamePh}
            className={`input pl-9 ${errors.tenantBusinessName ? 'input-error' : ''}`}
          />
        </Field>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <Field label={t.address1} error={errors.mailingAddress1} icon={<MapPin className="w-4 h-4" />}>
            <input
              type="text"
              value={form.mailingAddress1}
              onChange={onChange('mailingAddress1')}
              placeholder={t.address1Ph}
              className={`input pl-9 ${errors.mailingAddress1 ? 'input-error' : ''}`}
            />
          </Field>
          <Field label={t.address2} icon={<MapPin className="w-4 h-4" />}>
            <input
              type="text"
              value={form.mailingAddress2}
              onChange={onChange('mailingAddress2')}
              placeholder={t.address2Ph}
              className="input pl-9"
            />
          </Field>
        </div>

        <Field label={t.contactPerson} error={errors.contactPerson} icon={<User className="w-4 h-4" />}>
          <input
            type="text"
            value={form.contactPerson}
            onChange={onChange('contactPerson')}
            placeholder={t.contactPersonPh}
            className={`input pl-9 ${errors.contactPerson ? 'input-error' : ''}`}
          />
        </Field>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          {/* Primary phone with live uniqueness indicator */}
          <div>
            <Field label={t.phone1} error={errors.contactNumber1} icon={<Phone className="w-4 h-4" />}>
              <input
                type="tel"
                value={form.contactNumber1}
                onChange={onChange('contactNumber1')}
                placeholder={t.phone1Ph}
                className={`input pl-9 ${errors.contactNumber1 ? 'input-error' : ''}`}
              />
            </Field>
            {!errors.contactNumber1 && (
              <div className="min-h-[1.25rem] mt-1">
                {mobileChecking ? (
                  <p className="text-xs text-gray-400 flex items-center gap-1">
                    <Loader2 className="w-3 h-3 animate-spin" /> {t.mobileChecking}
                  </p>
                ) : mobileStatus?.available === true ? (
                  <p className="text-xs text-green-600 flex items-center gap-1">
                    <CheckCircle className="w-3 h-3" /> {t.mobileAvailable}
                  </p>
                ) : mobileStatus?.available === false ? (
                  <p className="text-xs text-red-500 flex items-center gap-1">
                    <AlertCircle className="w-3 h-3" /> {t.errMobileTaken}
                  </p>
                ) : null}
              </div>
            )}
          </div>

          <Field label={t.phone2} icon={<Phone className="w-4 h-4" />}>
            <input
              type="tel"
              value={form.contactNumber2}
              onChange={onChange('contactNumber2')}
              placeholder={t.phone2Ph}
              className="input pl-9"
            />
          </Field>
        </div>

        {/* Email with live uniqueness indicator */}
        <div>
          <Field label={t.email} error={errors.emailAddress} icon={<Mail className="w-4 h-4" />}>
            <input
              type="email"
              value={form.emailAddress}
              onChange={onChange('emailAddress')}
              placeholder={t.emailPh}
              className={`input pl-9 ${errors.emailAddress ? 'input-error' : ''}`}
            />
          </Field>
          {!errors.emailAddress && (
            <div className="min-h-[1.25rem] mt-1">
              {emailChecking ? (
                <p className="text-xs text-gray-400 flex items-center gap-1">
                  <Loader2 className="w-3 h-3 animate-spin" /> {t.emailChecking}
                </p>
              ) : emailStatus?.available === true ? (
                <p className="text-xs text-green-600 flex items-center gap-1">
                  <CheckCircle className="w-3 h-3" /> {t.emailAvailable}
                </p>
              ) : emailStatus?.available === false ? (
                <p className="text-xs text-red-500 flex items-center gap-1">
                  <AlertCircle className="w-3 h-3" /> {t.errEmailTaken}
                </p>
              ) : null}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

// ─── Step 2: Package Selection ─────────────────────────────────────────────────
function Step2({ t, form, errors, onChange, packages, pkgLoading, selectedPackage }) {
  return (
    <div>
      <h2 className="text-xl font-bold text-gray-900">{t.s2Title}</h2>
      <p className="text-sm text-gray-500 mt-1 mb-6">{t.s2Subtitle}</p>

      <Field label={t.productCount} error={errors.maxInventoryItems || errors.packageCode} icon={<ShoppingBag className="w-4 h-4" />}>
        <input
          type="number"
          min="1"
          value={form.maxInventoryItems}
          onChange={onChange('maxInventoryItems')}
          placeholder={t.productCountPh}
          className={`input pl-9 ${errors.maxInventoryItems || errors.packageCode ? 'input-error' : ''}`}
        />
      </Field>

      <p className="text-xs text-gray-400 mt-1.5 flex items-center gap-1">
        <Info className="w-3 h-3" /> {t.productCountHint}
      </p>

      {/* Package display */}
      <div className="mt-6">
        {pkgLoading ? (
          <div className="flex items-center justify-center py-8 text-gray-400 gap-2">
            <Loader2 className="w-5 h-5 animate-spin" />
            <span className="text-sm">{t.loadingPkg}</span>
          </div>
        ) : form.maxInventoryItems && !selectedPackage ? (
          <div className="p-4 bg-yellow-50 border border-yellow-200 rounded-xl flex items-start gap-2">
            <AlertCircle className="w-4 h-4 text-yellow-600 mt-0.5 shrink-0" />
            <p className="text-sm text-yellow-800">{t.noPackage}</p>
          </div>
        ) : selectedPackage ? (
          <div className="p-5 bg-primary-50 border-2 border-primary-300 rounded-xl">
            <div className="flex items-center gap-2 mb-4">
              <div className="w-9 h-9 bg-primary-600 rounded-lg flex items-center justify-center">
                <Package className="w-5 h-5 text-white" />
              </div>
              <div>
                <p className="text-xs font-medium text-primary-600 uppercase tracking-wider">{t.recommendedPkg}</p>
                <p className="font-bold text-gray-900 text-lg leading-tight">{selectedPackage.packageName}</p>
              </div>
              <span className="ml-auto font-mono text-xs bg-primary-100 text-primary-700 px-2 py-1 rounded">
                {selectedPackage.packageCode}
              </span>
            </div>
            <div className="grid grid-cols-3 gap-3">
              <div className="bg-white rounded-lg p-3 text-center">
                <p className="text-xs text-gray-500 mb-1">{t.productRange}</p>
                <p className="font-semibold text-gray-800 text-sm">
                  {selectedPackage.productCountFrom.toLocaleString()} – {selectedPackage.productCountTo.toLocaleString()}
                </p>
                <p className="text-xs text-gray-400">{t.products}</p>
              </div>
              <div className="bg-white rounded-lg p-3 text-center">
                <p className="text-xs text-gray-500 mb-1">{t.registrationFee}</p>
                <p className="font-bold text-gray-900 text-base">৳ {formatCurrency(selectedPackage.registrationFee)}</p>
              </div>
              <div className="bg-white rounded-lg p-3 text-center">
                <p className="text-xs text-gray-500 mb-1">{t.monthlyFee}</p>
                <p className="font-bold text-primary-700 text-base">৳ {formatCurrency(selectedPackage.monthlyFee)}</p>
              </div>
            </div>
          </div>
        ) : null}
      </div>
    </div>
  )
}

// ─── Step 3: Domain Setup ──────────────────────────────────────────────────────
function Step3({ t, form, errors, onChange, domainStatus, domainChecking }) {
  return (
    <div>
      <h2 className="text-xl font-bold text-gray-900">{t.s3Title}</h2>
      <p className="text-sm text-gray-500 mt-1 mb-6">{t.s3Subtitle}</p>

      <div>
        <label className="label">{t.domainPrefix}</label>
        {/* Domain input with live preview */}
        <div className="flex items-center rounded-lg border border-gray-300 focus-within:ring-2 focus-within:ring-primary-500 focus-within:border-transparent overflow-hidden bg-white transition-all">
          <div className="bg-gray-50 border-r border-gray-300 px-3 py-2 flex items-center">
            <Globe className="w-4 h-4 text-gray-400 mr-1" />
          </div>
          <input
            type="text"
            value={form.domainPrefix}
            onChange={onChange('domainPrefix')}
            placeholder={t.domainPrefixPh}
            className={`flex-1 px-3 py-2 text-sm outline-none bg-transparent ${errors.domainPrefix ? 'text-red-700' : ''}`}
          />
          <span className="bg-gray-50 border-l border-gray-300 px-3 py-2 text-sm text-gray-400 whitespace-nowrap">
            .myonline.com
          </span>
        </div>

        {/* Domain status feedback */}
        <div className="mt-2 min-h-[1.25rem]">
          {domainChecking ? (
            <p className="text-xs text-gray-400 flex items-center gap-1">
              <Loader2 className="w-3 h-3 animate-spin" /> {t.domainChecking}
            </p>
          ) : domainStatus?.available ? (
            <p className="text-xs text-green-600 flex items-center gap-1">
              <CheckCircle className="w-3 h-3" /> {t.domainAvailable}
            </p>
          ) : domainStatus && !domainStatus.available ? (
            <p className="text-xs text-red-500 flex items-center gap-1">
              <AlertCircle className="w-3 h-3" /> {t.domainTaken}
            </p>
          ) : null}
        </div>

        {errors.domainPrefix && (
          <p className="text-xs text-red-500 mt-1">{errors.domainPrefix}</p>
        )}

        <p className="text-xs text-gray-400 mt-2 flex items-center gap-1">
          <Info className="w-3 h-3 shrink-0" /> {t.domainRules}
        </p>
      </div>

      {/* URL preview */}
      {form.domainPrefix && (
        <div className="mt-5 p-4 bg-gray-50 border border-gray-200 rounded-xl">
          <p className="text-xs text-gray-500 mb-1">{t.domainPrefixInfo}:</p>
          <p className="font-mono font-semibold text-primary-700 text-base break-all">
            https://{form.domainPrefix || '…'}.myonline.com
          </p>
        </div>
      )}
    </div>
  )
}

// ─── Step 4: Payment Information ───────────────────────────────────────────────
function Step4({ t, form, errors, onChange, receiptFileName, onFileChange, onRemoveFile, fileInputRef, selectedPackage }) {
  return (
    <div>
      <h2 className="text-xl font-bold text-gray-900">{t.s4Title}</h2>
      <p className="text-sm text-gray-500 mt-1 mb-2">{t.s4Subtitle}</p>

      <div className="p-3 bg-blue-50 border border-blue-100 rounded-lg mb-5 flex items-start gap-2">
        <Info className="w-4 h-4 text-blue-500 mt-0.5 shrink-0" />
        <p className="text-xs text-blue-700">{t.paymentNote}</p>
      </div>

      {/* Payment summary */}
      {selectedPackage && (
        <div className="mb-5 p-4 bg-gray-50 border border-gray-200 rounded-xl flex items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 bg-primary-100 rounded-lg flex items-center justify-center">
              <CreditCard className="w-5 h-5 text-primary-600" />
            </div>
            <div>
              <p className="text-xs text-gray-500">{selectedPackage.packageName}</p>
              <p className="font-bold text-gray-900">৳ {formatCurrency(selectedPackage.registrationFee)}</p>
            </div>
          </div>
          <p className="text-xs text-gray-400">{t.registrationFee}</p>
        </div>
      )}

      <div className="space-y-4">
        <Field label={t.paymentChannel} icon={<CreditCard className="w-4 h-4" />}>
          <input
            type="text"
            value={form.registrationFeePmtChannel}
            onChange={onChange('registrationFeePmtChannel')}
            placeholder={t.paymentChannelPh}
            className="input pl-9"
          />
        </Field>

        <Field label={t.paymentRef} icon={<Search className="w-4 h-4" />}>
          <input
            type="text"
            value={form.registrationFeePmtRef}
            onChange={onChange('registrationFeePmtRef')}
            placeholder={t.paymentRefPh}
            className="input pl-9"
          />
        </Field>

        {/* Receipt upload */}
        <div>
          <label className="label">{t.paymentReceipt}</label>
          {receiptFileName ? (
            <div className="flex items-center gap-2 p-3 bg-green-50 border border-green-200 rounded-lg">
              <CheckCircle className="w-4 h-4 text-green-600 shrink-0" />
              <span className="text-sm text-green-800 flex-1 truncate">{receiptFileName}</span>
              <button
                type="button"
                onClick={onRemoveFile}
                className="text-red-500 hover:text-red-700 transition-colors"
              >
                <X className="w-4 h-4" />
              </button>
            </div>
          ) : (
            <label className="flex flex-col items-center justify-center p-6 border-2 border-dashed border-gray-300 rounded-xl cursor-pointer hover:border-primary-400 hover:bg-primary-50 transition-all">
              <Upload className="w-8 h-8 text-gray-400 mb-2" />
              <span className="text-sm font-medium text-gray-600">{t.uploadReceipt}</span>
              <span className="text-xs text-gray-400 mt-1">{t.receiptFormats}</span>
              <input
                ref={fileInputRef}
                type="file"
                accept="image/png,image/jpeg,image/jpg"
                onChange={onFileChange}
                className="hidden"
              />
            </label>
          )}
          {errors.receipt && (
            <p className="text-xs text-red-500 mt-1">{errors.receipt}</p>
          )}
          <p className="text-xs text-gray-400 mt-1.5 flex items-center gap-1">
            <Info className="w-3 h-3" /> {t.paymentReceiptHint}
          </p>
        </div>
      </div>
    </div>
  )
}

// ─── Success Screen ────────────────────────────────────────────────────────────
function SuccessScreen({ t, reg, pkg, onReset }) {
  return (
    <main className="flex-1 flex items-center justify-center px-4 py-12">
      <div className="w-full max-w-lg bg-white rounded-2xl shadow-sm border border-gray-100 p-8 text-center">
        <div className="w-20 h-20 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-5">
          <CheckCircle className="w-10 h-10 text-green-600" />
        </div>
        <h2 className="text-2xl font-bold text-gray-900 mb-2">{t.successTitle}</h2>
        <p className="text-gray-500 text-sm mb-6 leading-relaxed">{t.successMsg}</p>

        <div className="bg-gray-50 rounded-xl p-5 text-left space-y-3 mb-6 border border-gray-100">
          <InfoRow label={t.regId} value={`#${reg.id}`} mono />
          <InfoRow label={t.yourDomain} value={`${reg.domainPrefix}.myonline.com`} mono />
          <InfoRow label={t.yourPkg} value={pkg?.packageName || reg.packageCode} />
        </div>

        <div className="flex flex-col sm:flex-row gap-3">
          <button
            onClick={onReset}
            className="flex-1 px-4 py-2 rounded-lg border border-gray-300 text-sm font-medium text-gray-700 hover:bg-gray-50 transition-colors"
          >
            {t.registerAnother}
          </button>
          <a
            href="/login"
            className="flex-1 px-4 py-2 rounded-lg bg-primary-600 text-white text-sm font-medium hover:bg-primary-700 transition-colors flex items-center justify-center gap-1.5"
          >
            {t.goAdmin}
          </a>
        </div>
      </div>
    </main>
  )
}

function InfoRow({ label, value, mono }) {
  return (
    <div className="flex items-center justify-between gap-2">
      <span className="text-sm text-gray-500">{label}</span>
      <span className={`text-sm font-semibold text-gray-900 ${mono ? 'font-mono' : ''}`}>{value}</span>
    </div>
  )
}

// ─── Field wrapper ─────────────────────────────────────────────────────────────
function Field({ label, error, icon, children }) {
  return (
    <div>
      <label className="label">{label}</label>
      <div className="relative">
        {icon && (
          <span className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 pointer-events-none">
            {icon}
          </span>
        )}
        {children}
      </div>
      {error && <p className="text-xs text-red-500 mt-1">{error}</p>}
    </div>
  )
}
