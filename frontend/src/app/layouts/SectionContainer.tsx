import { type ReactNode } from 'react'

const SectionContainer = ({children} : {children: ReactNode}) => {
  return (
    <section className='lg:px-14 md:px-10 sm:py-25 sm:px-8 px-6 py-15 relative z-30 w-full relative'>
      {children}
    </section>
  )
}

export default SectionContainer