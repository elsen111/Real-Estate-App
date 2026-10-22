import { type ReactNode } from 'react'

const SectionContainer = ({children} : {children: ReactNode}) => {
  return (
    <section className='lg:px-14 md:px-10 sm:px-8 px-6 relative z-30 w-full'>
      {children}
    </section>
  )
}

export default SectionContainer